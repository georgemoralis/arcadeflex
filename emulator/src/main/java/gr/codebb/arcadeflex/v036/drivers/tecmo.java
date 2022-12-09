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
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.vidhrdw.tecmo.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.sound.adpcm.*;
import static arcadeflex.v036.sound.adpcmH.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intf.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intfH.*;

public class tecmo {

    public static WriteHandlerPtr tecmo_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU1);

            bankaddress = 0x10000 + ((data & 0xf8) << 8);
            cpu_setbank(1, new UBytePtr(RAM, bankaddress));
        }
    };

    public static WriteHandlerPtr tecmo_sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(offset, data);
            cpu_cause_interrupt(1, Z80_NMI_INT);
        }
    };

    static int adpcm_start, adpcm_end;

    public static WriteHandlerPtr tecmo_adpcm_start_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            adpcm_start = data << 8;
        }
    };
    public static WriteHandlerPtr tecmo_adpcm_end_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            adpcm_end = (data + 1) << 8;
        }
    };
    public static WriteHandlerPtr tecmo_adpcm_trigger_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            ADPCM_setvol(0, (data & 0x0f) * 0x11);
            if ((data & 0x0f) != 0) /* maybe this selects the volume? */ {
                if (adpcm_start < 0x8000) {
                    ADPCM_play(0, adpcm_start, (adpcm_end - adpcm_start) * 2);
                }
            }
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xefff, MRA_RAM),
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_BANK1),
                new MemoryReadAddress(0xf800, 0xf800, input_port_0_r),
                new MemoryReadAddress(0xf801, 0xf801, input_port_1_r),
                new MemoryReadAddress(0xf802, 0xf802, input_port_2_r),
                new MemoryReadAddress(0xf803, 0xf803, input_port_3_r),
                new MemoryReadAddress(0xf804, 0xf804, input_port_4_r),
                new MemoryReadAddress(0xf805, 0xf805, input_port_5_r),
                new MemoryReadAddress(0xf806, 0xf806, input_port_6_r),
                new MemoryReadAddress(0xf807, 0xf807, input_port_7_r),
                new MemoryReadAddress(0xf808, 0xf808, input_port_8_r),
                new MemoryReadAddress(0xf809, 0xf809, input_port_9_r),
                new MemoryReadAddress(0xf80f, 0xf80f, input_port_10_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress silkworm_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xc1ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xc200, 0xc3ff, colorram_w, colorram),
                new MemoryWriteAddress(0xc400, 0xc5ff, tecmo_videoram_w, tecmo_videoram),
                new MemoryWriteAddress(0xc600, 0xc7ff, tecmo_colorram_w, tecmo_colorram),
                new MemoryWriteAddress(0xc800, 0xcbff, MWA_RAM, tecmo_videoram2, tecmo_videoram2_size),
                new MemoryWriteAddress(0xcc00, 0xcfff, MWA_RAM, tecmo_colorram2),
                new MemoryWriteAddress(0xd000, 0xdfff, MWA_RAM),
                new MemoryWriteAddress(0xe000, 0xe7ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xe800, 0xefff, paletteram_xxxxBBBBRRRRGGGG_swap_w, paletteram),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_ROM),
                new MemoryWriteAddress(0xf800, 0xf805, MWA_RAM, tecmo_scroll),
                new MemoryWriteAddress(0xf806, 0xf806, tecmo_sound_command_w),
                new MemoryWriteAddress(0xf807, 0xf807, MWA_NOP), /* ???? */
                new MemoryWriteAddress(0xf808, 0xf808, tecmo_bankswitch_w),
                new MemoryWriteAddress(0xf809, 0xf809, MWA_NOP), /* ???? */
                new MemoryWriteAddress(0xf80b, 0xf80b, MWA_NOP), /* ???? */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress rygar_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xcfff, MWA_RAM),
                new MemoryWriteAddress(0xd000, 0xd3ff, MWA_RAM, tecmo_videoram2, tecmo_videoram2_size),
                new MemoryWriteAddress(0xd400, 0xd7ff, MWA_RAM, tecmo_colorram2),
                new MemoryWriteAddress(0xd800, 0xd9ff, tecmo_videoram_w, tecmo_videoram),
                new MemoryWriteAddress(0xda00, 0xdbff, tecmo_colorram_w, tecmo_colorram),
                new MemoryWriteAddress(0xdc00, 0xddff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xde00, 0xdfff, colorram_w, colorram),
                new MemoryWriteAddress(0xe000, 0xe7ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xe800, 0xefff, paletteram_xxxxBBBBRRRRGGGG_swap_w, paletteram),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_ROM),
                new MemoryWriteAddress(0xf800, 0xf805, MWA_RAM, tecmo_scroll),
                new MemoryWriteAddress(0xf806, 0xf806, tecmo_sound_command_w),
                new MemoryWriteAddress(0xf807, 0xf807, MWA_NOP), /* ???? */
                new MemoryWriteAddress(0xf808, 0xf808, tecmo_bankswitch_w),
                new MemoryWriteAddress(0xf809, 0xf809, MWA_NOP), /* ???? */
                new MemoryWriteAddress(0xf80b, 0xf80b, MWA_NOP), /* ???? */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress gemini_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xcfff, MWA_RAM),
                new MemoryWriteAddress(0xd000, 0xd3ff, MWA_RAM, tecmo_videoram2, tecmo_videoram2_size),
                new MemoryWriteAddress(0xd400, 0xd7ff, MWA_RAM, tecmo_colorram2),
                new MemoryWriteAddress(0xd800, 0xd9ff, tecmo_videoram_w, tecmo_videoram),
                new MemoryWriteAddress(0xda00, 0xdbff, tecmo_colorram_w, tecmo_colorram),
                new MemoryWriteAddress(0xdc00, 0xddff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xde00, 0xdfff, colorram_w, colorram),
                new MemoryWriteAddress(0xe000, 0xe7ff, paletteram_xxxxBBBBRRRRGGGG_swap_w, paletteram),
                new MemoryWriteAddress(0xe800, 0xefff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_ROM),
                new MemoryWriteAddress(0xf800, 0xf805, MWA_RAM, tecmo_scroll),
                new MemoryWriteAddress(0xf806, 0xf806, tecmo_sound_command_w),
                new MemoryWriteAddress(0xf807, 0xf807, MWA_NOP), /* ???? */
                new MemoryWriteAddress(0xf808, 0xf808, tecmo_bankswitch_w),
                new MemoryWriteAddress(0xf809, 0xf809, MWA_NOP), /* ???? */
                new MemoryWriteAddress(0xf80b, 0xf80b, MWA_NOP), /* ???? */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0xc000, 0xc000, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x2000, 0x207f, MWA_RAM), /* Silkworm set #2 has a custom CPU which */
                /* writes code to this area */
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0xa000, 0xa000, YM3812_control_port_0_w),
                new MemoryWriteAddress(0xa001, 0xa001, YM3812_write_port_0_w),
                new MemoryWriteAddress(0xc000, 0xc000, tecmo_adpcm_start_w),
                new MemoryWriteAddress(0xc400, 0xc400, tecmo_adpcm_end_w),
                new MemoryWriteAddress(0xc800, 0xc800, tecmo_adpcm_trigger_w),
                new MemoryWriteAddress(0xcc00, 0xcc00, MWA_NOP), /* NMI acknowledge? */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress rygar_sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x47ff, MRA_RAM),
                new MemoryReadAddress(0xc000, 0xc000, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress rygar_sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x47ff, MWA_RAM),
                /*TODO*///		new MemoryWriteAddress( 0x8000, 0x8000, YM3812_control_port_0_w ),
                /*TODO*///		new MemoryWriteAddress( 0x8001, 0x8001, YM3812_write_port_0_w ),
                new MemoryWriteAddress(0xc000, 0xc000, tecmo_adpcm_start_w),
                new MemoryWriteAddress(0xd000, 0xd000, tecmo_adpcm_end_w),
                new MemoryWriteAddress(0xe000, 0xe000, tecmo_adpcm_trigger_w),
                new MemoryWriteAddress(0xf000, 0xf000, MWA_NOP), /* NMI acknowledge? */
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_rygar = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 bits 0-3 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);

            PORT_START(); 	/* IN1 bits 0-3 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 	/* IN2 bits 0-3 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);

            PORT_START(); 	/* IN3 bits 0-3 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 	/* IN4 bits 0-3 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);

            PORT_START(); 	/* unused? */

            PORT_BIT(0x0f, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 	/* DSWA bit 0-3 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x0C, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0C, DEF_STR("1C_3C"));

            PORT_START(); 	/* DSWA bit 4-7 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x04, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));

            PORT_START(); 	/* DSWB bit 0-3 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "50000 200000 500000");
            PORT_DIPSETTING(0x01, "100000 300000 600000");
            PORT_DIPSETTING(0x02, "200000 500000");
            PORT_DIPSETTING(0x03, "100000");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));

            PORT_START(); 	/* DSWB bit 4-7 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x01, "Normal");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");
            PORT_DIPNAME(0x04, 0x00, "2P Can Start Anytime");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x04, DEF_STR("Yes"));
            PORT_DIPNAME(0x08, 0x08, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x08, DEF_STR("Yes"));

            PORT_START(); 	/* unused? */

            PORT_BIT(0x0f, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_gemini = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 bits 0-3 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);

            PORT_START(); 	/* IN1 bits 0-3 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 	/* IN2 bits 0-3 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);

            PORT_START(); 	/* IN3 bits 0-3 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 	/* unused? */

            PORT_BIT(0x0f, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 	/* IN4 bits 0-3 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN2);

            PORT_START(); 	/* DSWA bit 0-3 */

            PORT_DIPNAME(0x07, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x08, 0x00, "Final Round Continuation");
            PORT_DIPSETTING(0x00, "Round 6");
            PORT_DIPSETTING(0x08, "Round 7");

            PORT_START(); 	/* DSWA bit 4-7 */

            PORT_DIPNAME(0x07, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x08, 0x00, "Buy in During Final Round");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x08, DEF_STR("Yes"));

            PORT_START(); 	/* DSWB bit 0-3 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x04, "Normal");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPSETTING(0x0c, "Hardest");

            PORT_START(); 	/* DSWB bit 4-7 */

            PORT_DIPNAME(0x07, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "50000 200000");
            PORT_DIPSETTING(0x01, "50000 300000");
            PORT_DIPSETTING(0x02, "100000 500000");
            PORT_DIPSETTING(0x03, "50000");
            PORT_DIPSETTING(0x04, "100000");
            PORT_DIPSETTING(0x05, "200000");
            PORT_DIPSETTING(0x06, "300000");
            PORT_DIPSETTING(0x07, "None");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* unused? */

            PORT_BIT(0x0f, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_silkworm = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 bit 0-3 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);

            PORT_START(); 	/* IN0 bit 4-7 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON3);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* unused? */

            PORT_START(); 	/* IN1 bit 0-3 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);

            PORT_START(); 	/* IN1 bit 4-7 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* unused? */

            PORT_START(); 	/* unused? */

            PORT_BIT(0x0f, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 	/* unused? */

            PORT_BIT(0x0f, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 	/* DSWA bit 0-3 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x0C, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0C, DEF_STR("1C_3C"));

            PORT_START(); 	/* DSWA bit 4-7 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPNAME(0x04, 0x00, "A 7");/* unused? */

            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));

            PORT_START(); 	/* DSWB bit 0-3 */

            PORT_DIPNAME(0x07, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "50000 200000 500000");
            PORT_DIPSETTING(0x01, "100000 300000 800000");
            PORT_DIPSETTING(0x02, "50000 200000");
            PORT_DIPSETTING(0x03, "100000 300000");
            PORT_DIPSETTING(0x04, "50000");
            PORT_DIPSETTING(0x05, "100000");
            PORT_DIPSETTING(0x06, "200000");
            PORT_DIPSETTING(0x07, "None");
            PORT_DIPNAME(0x08, 0x00, "B 4");/* unused? */

            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));

            PORT_START(); 	/* DSWB bit 4-7 */

            PORT_DIPNAME(0x07, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "0");
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_DIPSETTING(0x05, "5");
            /* 0x06 and 0x07 are the same as 0x00 */
            PORT_DIPNAME(0x08, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x08, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));

            PORT_START(); 	/* COIN */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN2);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout tecmo_charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            1024, /* 1024 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every char takes 32 consecutive bytes */
    );

    static GfxLayout silkworm_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            2048, /* 2048 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4,
                32 * 8 + 0 * 4, 32 * 8 + 1 * 4, 32 * 8 + 2 * 4, 32 * 8 + 3 * 4, 32 * 8 + 4 * 4, 32 * 8 + 5 * 4, 32 * 8 + 6 * 4, 32 * 8 + 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                16 * 32, 17 * 32, 18 * 32, 19 * 32, 20 * 32, 21 * 32, 22 * 32, 23 * 32},
            128 * 8 /* every sprite takes 128 consecutive bytes */
    );

    static GfxLayout silkworm_spritelayout2x = new GfxLayout(
            32, 32, /* 32*32 sprites */
            512, /* 512 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4,
                32 * 8 + 0 * 4, 32 * 8 + 1 * 4, 32 * 8 + 2 * 4, 32 * 8 + 3 * 4, 32 * 8 + 4 * 4, 32 * 8 + 5 * 4, 32 * 8 + 6 * 4, 32 * 8 + 7 * 4,
                128 * 8 + 0 * 4, 128 * 8 + 1 * 4, 128 * 8 + 2 * 4, 128 * 8 + 3 * 4, 128 * 8 + 4 * 4, 128 * 8 + 5 * 4, 128 * 8 + 6 * 4, 128 * 8 + 7 * 4,
                160 * 8 + 0 * 4, 160 * 8 + 1 * 4, 160 * 8 + 2 * 4, 160 * 8 + 3 * 4, 160 * 8 + 4 * 4, 160 * 8 + 5 * 4, 160 * 8 + 6 * 4, 160 * 8 + 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                16 * 32, 17 * 32, 18 * 32, 19 * 32, 20 * 32, 21 * 32, 22 * 32, 23 * 32,
                64 * 32, 65 * 32, 66 * 32, 67 * 32, 68 * 32, 69 * 32, 70 * 32, 71 * 32,
                80 * 32, 81 * 32, 82 * 32, 83 * 32, 84 * 32, 85 * 32, 86 * 32, 87 * 32},
            512 * 8 /* every sprite takes 512 consecutive bytes */
    );

    static GfxLayout silkworm_spritelayout8x8 = new GfxLayout(
            8, 8, /* 8*8 xprites */
            8192, /* 8192 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    /* the only difference in rygar_spritelayout is that half as many sprites are present */
    static GfxLayout rygar_spritelayout = new GfxLayout/* only difference is half as many sprites as silkworm */(
                    16, 16, /* 16*16 sprites */
                    1024, /* 1024 sprites */
                    4, /* 4 bits per pixel */
                    new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
                    new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4,
                        32 * 8 + 0 * 4, 32 * 8 + 1 * 4, 32 * 8 + 2 * 4, 32 * 8 + 3 * 4, 32 * 8 + 4 * 4, 32 * 8 + 5 * 4, 32 * 8 + 6 * 4, 32 * 8 + 7 * 4},
                    new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                        16 * 32, 17 * 32, 18 * 32, 19 * 32, 20 * 32, 21 * 32, 22 * 32, 23 * 32},
                    128 * 8 /* every sprite takes 128 consecutive bytes */
            );

    static GfxLayout rygar_spritelayout2x = new GfxLayout(
            32, 32, /* 32*32 sprites */
            256, /* 512 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4,
                32 * 8 + 0 * 4, 32 * 8 + 1 * 4, 32 * 8 + 2 * 4, 32 * 8 + 3 * 4, 32 * 8 + 4 * 4, 32 * 8 + 5 * 4, 32 * 8 + 6 * 4, 32 * 8 + 7 * 4,
                128 * 8 + 0 * 4, 128 * 8 + 1 * 4, 128 * 8 + 2 * 4, 128 * 8 + 3 * 4, 128 * 8 + 4 * 4, 128 * 8 + 5 * 4, 128 * 8 + 6 * 4, 128 * 8 + 7 * 4,
                160 * 8 + 0 * 4, 160 * 8 + 1 * 4, 160 * 8 + 2 * 4, 160 * 8 + 3 * 4, 160 * 8 + 4 * 4, 160 * 8 + 5 * 4, 160 * 8 + 6 * 4, 160 * 8 + 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                16 * 32, 17 * 32, 18 * 32, 19 * 32, 20 * 32, 21 * 32, 22 * 32, 23 * 32,
                64 * 32, 65 * 32, 66 * 32, 67 * 32, 68 * 32, 69 * 32, 70 * 32, 71 * 32,
                80 * 32, 81 * 32, 82 * 32, 83 * 32, 84 * 32, 85 * 32, 86 * 32, 87 * 32},
            512 * 8 /* every sprite takes 512 consecutive bytes */
    );

    static GfxLayout rygar_spritelayout8x8 = new GfxLayout(
            8, 8, /* 8*8 xprites */
            4096, /* 8192 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, tecmo_charlayout, 256, 16), /* colors 256 - 511 */
                new GfxDecodeInfo(REGION_GFX2, 0, silkworm_spritelayout8x8, 0, 16), /* colors   0 - 255 */
                new GfxDecodeInfo(REGION_GFX2, 0, silkworm_spritelayout, 0, 16), /* 16x16 sprites */
                new GfxDecodeInfo(REGION_GFX2, 0, silkworm_spritelayout2x, 0, 16), /* double size hack */
                new GfxDecodeInfo(REGION_GFX3, 0, silkworm_spritelayout, 512, 16), /* bg#1 colors 512 - 767 */
                new GfxDecodeInfo(REGION_GFX4, 0, silkworm_spritelayout, 768, 16), /* bg#2 colors 768 - 1023 */
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo rygar_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, tecmo_charlayout, 256, 16), /* colors 256 - 511 */
                new GfxDecodeInfo(REGION_GFX2, 0, rygar_spritelayout8x8, 0, 16), /* colors   0 - 255 */
                new GfxDecodeInfo(REGION_GFX2, 0, rygar_spritelayout, 0, 16), /* 16x16 sprites */
                new GfxDecodeInfo(REGION_GFX2, 0, rygar_spritelayout2x, 0, 16), /* double size hack */
                new GfxDecodeInfo(REGION_GFX3, 0, rygar_spritelayout, 512, 16), /* bg#1 colors 512 - 767 */
                new GfxDecodeInfo(REGION_GFX4, 0, rygar_spritelayout, 768, 16), /* bg#2 colors 768 - 1023 */
                new GfxDecodeInfo(-1) /* end of array */};

    static YM3526interface rygar_ym3812_interface = new YM3526interface(
            1, /* 1 chip (no more supported) */
            4000000, /* 4 MHz ? */
            new int[]{255} /* (not supported) */
    );

    static YM3526interface ym3812_interface = new YM3526interface(
            1, /* 1 chip (no more supported) */
            4000000, /* 4 MHz ? */
            new int[]{255} /* (not supported) */
    );

    /* ADPCM chip is a MSM5205 @ 400kHz */
    static ADPCMinterface adpcm_interface = new ADPCMinterface(
            1, /* 1 channel */
            8333, /* 8000Hz playback */
            REGION_SOUND1, /* memory region 3 */
            null, /* init function */
            new int[]{255}
    );

    static MachineDriver machine_driver_silkworm = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        7600000, /* 7.6 Mhz (?????) */
                        readmem, silkworm_writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4 MHz ???? */
                        sound_readmem, sound_writemem, null, null,
                        interrupt, 2 /* ?? */
                /* NMIs are triggered by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            1024, 1024,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            silkworm_vh_start,
            tecmo_vh_stop,
            tecmo_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),
                new MachineSound(
                        SOUND_ADPCM,
                        adpcm_interface
                )
            }
    );

    static MachineDriver machine_driver_rygar = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        7600000,
                        readmem, rygar_writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4 MHz ???? */
                        rygar_sound_readmem, rygar_sound_writemem, null, null,
                        interrupt, 2 /* ?? */
                /* NMIs are triggered by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            rygar_gfxdecodeinfo,
            1024, 1024,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            rygar_vh_start,
            tecmo_vh_stop,
            tecmo_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        rygar_ym3812_interface
                ),
                new MachineSound(
                        SOUND_ADPCM,
                        adpcm_interface
                ),}
    );

    static MachineDriver machine_driver_gemini = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        7600000, /* 7.6 Mhz (?????) */
                        readmem, gemini_writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4 MHz ???? */
                        sound_readmem, sound_writemem, null, null,
                        interrupt, 2 /* ?? */
                /* NMIs are triggered by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            1024, 1024,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            gemini_vh_start,
            tecmo_vh_stop,
            tecmo_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),
                new MachineSound(
                        SOUND_ADPCM,
                        adpcm_interface
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
    static RomLoadPtr rom_rygar = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x18000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("5.5p", 0x00000, 0x08000, 0x062cd55d);/* code */

            ROM_LOAD("cpu_5m.bin", 0x08000, 0x04000, 0x7ac5191b);/* code */

            ROM_LOAD("cpu_5j.bin", 0x10000, 0x08000, 0xed76d606);/* banked at f000-f7ff */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("cpu_4h.bin", 0x0000, 0x2000, 0xe4a2fa87);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("cpu_8k.bin", 0x00000, 0x08000, 0x4d482fb6);/* characters */

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vid_6k.bin", 0x00000, 0x08000, 0xaba6db9e);/* sprites */

            ROM_LOAD("vid_6j.bin", 0x08000, 0x08000, 0xae1f2ed6);/* sprites */

            ROM_LOAD("vid_6h.bin", 0x10000, 0x08000, 0x46d9e7df);/* sprites */

            ROM_LOAD("vid_6g.bin", 0x18000, 0x08000, 0x45839c9a);/* sprites */

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vid_6p.bin", 0x00000, 0x08000, 0x9eae5f8e);
            ROM_LOAD("vid_6o.bin", 0x08000, 0x08000, 0x5a10a396);
            ROM_LOAD("vid_6n.bin", 0x10000, 0x08000, 0x7b12cf3f);
            ROM_LOAD("vid_6l.bin", 0x18000, 0x08000, 0x3cea7eaa);

            ROM_REGION(0x20000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vid_6f.bin", 0x00000, 0x08000, 0x9840edd8);
            ROM_LOAD("vid_6e.bin", 0x08000, 0x08000, 0xff65e074);
            ROM_LOAD("vid_6c.bin", 0x10000, 0x08000, 0x89868c85);
            ROM_LOAD("vid_6b.bin", 0x18000, 0x08000, 0x35389a7b);

            ROM_REGION(0x4000, REGION_SOUND1);/* ADPCM samples */

            ROM_LOAD("cpu_1f.bin", 0x0000, 0x4000, 0x3cc98c5a);
            ROM_END();
        }
    };

    static RomLoadPtr rom_rygar2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x18000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("cpu_5p.bin", 0x00000, 0x08000, 0xe79c054a);/* code */

            ROM_LOAD("cpu_5m.bin", 0x08000, 0x04000, 0x7ac5191b);/* code */

            ROM_LOAD("cpu_5j.bin", 0x10000, 0x08000, 0xed76d606);/* banked at f000-f7ff */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("cpu_4h.bin", 0x0000, 0x2000, 0xe4a2fa87);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("cpu_8k.bin", 0x00000, 0x08000, 0x4d482fb6);/* characters */

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vid_6k.bin", 0x00000, 0x08000, 0xaba6db9e);/* sprites */

            ROM_LOAD("vid_6j.bin", 0x08000, 0x08000, 0xae1f2ed6);/* sprites */

            ROM_LOAD("vid_6h.bin", 0x10000, 0x08000, 0x46d9e7df);/* sprites */

            ROM_LOAD("vid_6g.bin", 0x18000, 0x08000, 0x45839c9a);/* sprites */

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vid_6p.bin", 0x00000, 0x08000, 0x9eae5f8e);
            ROM_LOAD("vid_6o.bin", 0x08000, 0x08000, 0x5a10a396);
            ROM_LOAD("vid_6n.bin", 0x10000, 0x08000, 0x7b12cf3f);
            ROM_LOAD("vid_6l.bin", 0x18000, 0x08000, 0x3cea7eaa);

            ROM_REGION(0x20000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vid_6f.bin", 0x00000, 0x08000, 0x9840edd8);
            ROM_LOAD("vid_6e.bin", 0x08000, 0x08000, 0xff65e074);
            ROM_LOAD("vid_6c.bin", 0x10000, 0x08000, 0x89868c85);
            ROM_LOAD("vid_6b.bin", 0x18000, 0x08000, 0x35389a7b);

            ROM_REGION(0x4000, REGION_SOUND1);/* ADPCM samples */

            ROM_LOAD("cpu_1f.bin", 0x0000, 0x4000, 0x3cc98c5a);
            ROM_END();
        }
    };

    static RomLoadPtr rom_rygarj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x18000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("cpuj_5p.bin", 0x00000, 0x08000, 0xb39698ba);/* code */

            ROM_LOAD("cpuj_5m.bin", 0x08000, 0x04000, 0x3f180979);/* code */

            ROM_LOAD("cpuj_5j.bin", 0x10000, 0x08000, 0x69e44e8f);/* banked at f000-f7ff */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("cpu_4h.bin", 0x0000, 0x2000, 0xe4a2fa87);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("cpuj_8k.bin", 0x00000, 0x08000, 0x45047707);/* characters */

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vid_6k.bin", 0x00000, 0x08000, 0xaba6db9e);/* sprites */

            ROM_LOAD("vid_6j.bin", 0x08000, 0x08000, 0xae1f2ed6);/* sprites */

            ROM_LOAD("vid_6h.bin", 0x10000, 0x08000, 0x46d9e7df);/* sprites */

            ROM_LOAD("vid_6g.bin", 0x18000, 0x08000, 0x45839c9a);/* sprites */

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vid_6p.bin", 0x00000, 0x08000, 0x9eae5f8e);
            ROM_LOAD("vid_6o.bin", 0x08000, 0x08000, 0x5a10a396);
            ROM_LOAD("vid_6n.bin", 0x10000, 0x08000, 0x7b12cf3f);
            ROM_LOAD("vid_6l.bin", 0x18000, 0x08000, 0x3cea7eaa);

            ROM_REGION(0x20000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vid_6f.bin", 0x00000, 0x08000, 0x9840edd8);
            ROM_LOAD("vid_6e.bin", 0x08000, 0x08000, 0xff65e074);
            ROM_LOAD("vid_6c.bin", 0x10000, 0x08000, 0x89868c85);
            ROM_LOAD("vid_6b.bin", 0x18000, 0x08000, 0x35389a7b);

            ROM_REGION(0x4000, REGION_SOUND1);/* ADPCM samples */

            ROM_LOAD("cpu_1f.bin", 0x0000, 0x4000, 0x3cc98c5a);
            ROM_END();
        }
    };

    static RomLoadPtr rom_silkworm = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("silkworm.4", 0x00000, 0x10000, 0xa5277cce);/* c000-ffff is not used */

            ROM_LOAD("silkworm.5", 0x10000, 0x10000, 0xa6c7bb51);/* banked at f000-f7ff */

            ROM_REGION(0x20000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("silkworm.3", 0x0000, 0x8000, 0xb589f587);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("silkworm.2", 0x00000, 0x08000, 0xe80a1cd9);/* characters */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("silkworm.6", 0x00000, 0x10000, 0x1138d159);/* sprites */

            ROM_LOAD("silkworm.7", 0x10000, 0x10000, 0xd96214f7);/* sprites */

            ROM_LOAD("silkworm.8", 0x20000, 0x10000, 0x0494b38e);/* sprites */

            ROM_LOAD("silkworm.9", 0x30000, 0x10000, 0x8ce3cdf5);/* sprites */

            ROM_REGION(0x40000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("silkworm.10", 0x00000, 0x10000, 0x8c7138bb);/* tiles #1 */

            ROM_LOAD("silkworm.11", 0x10000, 0x10000, 0x6c03c476);/* tiles #1 */

            ROM_LOAD("silkworm.12", 0x20000, 0x10000, 0xbb0f568f);/* tiles #1 */

            ROM_LOAD("silkworm.13", 0x30000, 0x10000, 0x773ad0a4);/* tiles #1 */

            ROM_REGION(0x40000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("silkworm.14", 0x00000, 0x10000, 0x409df64b);/* tiles #2 */

            ROM_LOAD("silkworm.15", 0x10000, 0x10000, 0x6e4052c9);/* tiles #2 */

            ROM_LOAD("silkworm.16", 0x20000, 0x10000, 0x9292ed63);/* tiles #2 */

            ROM_LOAD("silkworm.17", 0x30000, 0x10000, 0x3fa4563d);/* tiles #2 */

            ROM_REGION(0x8000, REGION_SOUND1);/* ADPCM samples */

            ROM_LOAD("silkworm.1", 0x0000, 0x8000, 0x5b553644);
            ROM_END();
        }
    };

    static RomLoadPtr rom_silkwrm2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("r4", 0x00000, 0x10000, 0x6df3df22);/* c000-ffff is not used */

            ROM_LOAD("silkworm.5", 0x10000, 0x10000, 0xa6c7bb51);/* banked at f000-f7ff */

            ROM_REGION(0x20000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("r3", 0x0000, 0x8000, 0xb79848d0);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("silkworm.2", 0x00000, 0x08000, 0xe80a1cd9);/* characters */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("silkworm.6", 0x00000, 0x10000, 0x1138d159);/* sprites */

            ROM_LOAD("silkworm.7", 0x10000, 0x10000, 0xd96214f7);/* sprites */

            ROM_LOAD("silkworm.8", 0x20000, 0x10000, 0x0494b38e);/* sprites */

            ROM_LOAD("silkworm.9", 0x30000, 0x10000, 0x8ce3cdf5);/* sprites */

            ROM_REGION(0x40000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("silkworm.10", 0x00000, 0x10000, 0x8c7138bb);/* tiles #1 */

            ROM_LOAD("silkworm.11", 0x10000, 0x10000, 0x6c03c476);/* tiles #1 */

            ROM_LOAD("silkworm.12", 0x20000, 0x10000, 0xbb0f568f);/* tiles #1 */

            ROM_LOAD("silkworm.13", 0x30000, 0x10000, 0x773ad0a4);/* tiles #1 */

            ROM_REGION(0x40000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("silkworm.14", 0x00000, 0x10000, 0x409df64b);/* tiles #2 */

            ROM_LOAD("silkworm.15", 0x10000, 0x10000, 0x6e4052c9);/* tiles #2 */

            ROM_LOAD("silkworm.16", 0x20000, 0x10000, 0x9292ed63);/* tiles #2 */

            ROM_LOAD("silkworm.17", 0x30000, 0x10000, 0x3fa4563d);/* tiles #2 */

            ROM_REGION(0x8000, REGION_SOUND1);/* ADPCM samples */

            ROM_LOAD("silkworm.1", 0x0000, 0x8000, 0x5b553644);
            ROM_END();
        }
    };

    static RomLoadPtr rom_gemini = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("gw04-5s.rom", 0x00000, 0x10000, 0xff9de855);/* c000-ffff is not used */

            ROM_LOAD("gw05-6s.rom", 0x10000, 0x10000, 0x5a6947a9);/* banked at f000-f7ff */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("gw03-5h.rom", 0x0000, 0x8000, 0x9bc79596);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gw02-3h.rom", 0x00000, 0x08000, 0x7acc8d35);/* characters */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gw06-1c.rom", 0x00000, 0x10000, 0x4ea51631);/* sprites */

            ROM_LOAD("gw07-1d.rom", 0x10000, 0x10000, 0xda42637e);/* sprites */

            ROM_LOAD("gw08-1f.rom", 0x20000, 0x10000, 0x0b4e8d70);/* sprites */

            ROM_LOAD("gw09-1h.rom", 0x30000, 0x10000, 0xb65c5e4c);/* sprites */

            ROM_REGION(0x40000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gw10-1n.rom", 0x00000, 0x10000, 0x5e84cd4f);/* tiles #1 */

            ROM_LOAD("gw11-2na.rom", 0x10000, 0x10000, 0x08b458e1);/* tiles #1 */

            ROM_LOAD("gw12-2nb.rom", 0x20000, 0x10000, 0x229c9714);/* tiles #1 */

            ROM_LOAD("gw13-3n.rom", 0x30000, 0x10000, 0xc5dfaf47);/* tiles #1 */

            ROM_REGION(0x40000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gw14-1r.rom", 0x00000, 0x10000, 0x9c10e5b5);/* tiles #2 */

            ROM_LOAD("gw15-2ra.rom", 0x10000, 0x10000, 0x4cd18cfa);/* tiles #2 */

            ROM_LOAD("gw16-2rb.rom", 0x20000, 0x10000, 0xf911c7be);/* tiles #2 */

            ROM_LOAD("gw17-3r.rom", 0x30000, 0x10000, 0x79a9ce25);/* tiles #2 */

            ROM_REGION(0x8000, REGION_SOUND1);/* ADPCM samples */

            ROM_LOAD("gw01-6a.rom", 0x0000, 0x8000, 0xd78afa05);
            ROM_END();
        }
    };

    public static GameDriver driver_rygar = new GameDriver("1986", "rygar", "tecmo.java", rom_rygar, null, machine_driver_rygar, input_ports_rygar, null, ROT0, "Tecmo", "Rygar (US set 1)", GAME_NO_COCKTAIL);
    public static GameDriver driver_rygar2 = new GameDriver("1986", "rygar2", "tecmo.java", rom_rygar2, driver_rygar, machine_driver_rygar, input_ports_rygar, null, ROT0, "Tecmo", "Rygar (US set 2)", GAME_NO_COCKTAIL);
    public static GameDriver driver_rygarj = new GameDriver("1986", "rygarj", "tecmo.java", rom_rygarj, driver_rygar, machine_driver_rygar, input_ports_rygar, null, ROT0, "Tecmo", "Argus no Senshi (Japan)", GAME_NO_COCKTAIL);
    public static GameDriver driver_gemini = new GameDriver("1987", "gemini", "tecmo.java", rom_gemini, null, machine_driver_gemini, input_ports_gemini, null, ROT90, "Tecmo", "Gemini Wing", GAME_NO_COCKTAIL);
    public static GameDriver driver_silkworm = new GameDriver("1988", "silkworm", "tecmo.java", rom_silkworm, null, machine_driver_silkworm, input_ports_silkworm, null, ROT0, "Tecmo", "Silkworm (set 1)", GAME_NO_COCKTAIL);
    public static GameDriver driver_silkwrm2 = new GameDriver("1988", "silkwrm2", "tecmo.java", rom_silkwrm2, driver_silkworm, machine_driver_silkworm, input_ports_silkworm, null, ROT0, "Tecmo", "Silkworm (set 2)", GAME_NO_COCKTAIL);
}
