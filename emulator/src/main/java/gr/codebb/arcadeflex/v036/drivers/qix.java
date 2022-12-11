/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static common.libc.cstring.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.qix.*;
import static arcadeflex.v036.sound.dacH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.machine.qix.*;
import static gr.codebb.arcadeflex.v036.machine._6821pia.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;

public class qix {

    static UBytePtr nvram = new UBytePtr();
    static int[] nvram_size = new int[1];

    public static nvramHandlerPtr nvram_handler = new nvramHandlerPtr() {
        public void handler(Object file, int read_or_write) {
            if (read_or_write!=0) {
                osd_fwrite(file, nvram, nvram_size[0]);
            } else if (file!=null) {
                osd_fread(file, nvram, nvram_size[0]);
            } else {
                memset(nvram, 0, nvram_size[0]);
            }
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x8000, 0x83ff, qix_sharedram_r),
                new MemoryReadAddress(0x8400, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0x8800, 0x8800, MRA_RAM), /* ACIA */
                new MemoryReadAddress(0x9000, 0x9003, pia_3_r),
                new MemoryReadAddress(0x9400, 0x9403, pia_0_r),
                new MemoryReadAddress(0x9900, 0x9903, pia_1_r),
                new MemoryReadAddress(0x9c00, 0x9FFF, pia_2_r),
                new MemoryReadAddress(0xa000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress zoo_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x03ff, qix_sharedram_r),
                new MemoryReadAddress(0x0400, 0x07ff, MRA_RAM),
                new MemoryReadAddress(0x1000, 0x1003, pia_3_r), /* Sound PIA */
                new MemoryReadAddress(0x1400, 0x1403, pia_0_r), /* Game PIA 1 - Player inputs, coin door switches */
                new MemoryReadAddress(0x1900, 0x1903, pia_1_r), /* Game PIA 2 */
                new MemoryReadAddress(0x1c00, 0x1fff, pia_2_r), /* Game PIA 3 - Player 2 */
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress readmem_video[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, qix_videoram_r),
                new MemoryReadAddress(0x8000, 0x83ff, qix_sharedram_r),
                new MemoryReadAddress(0x8400, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0x9400, 0x9400, qix_addresslatch_r),
                new MemoryReadAddress(0x9800, 0x9800, qix_scanline_r),
                new MemoryReadAddress(0xa000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress zoo_readmem_video[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, qix_videoram_r),
                new MemoryReadAddress(0x8000, 0x83ff, qix_sharedram_r),
                new MemoryReadAddress(0x8400, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0x9400, 0x9400, qix_addresslatch_r),
                new MemoryReadAddress(0x9800, 0x9800, qix_scanline_r),
                new MemoryReadAddress(0xa000, 0xbfff, MRA_BANK1),
                new MemoryReadAddress(0xc000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress readmem_sound[]
            = {
                new MemoryReadAddress(0x0000, 0x007f, MRA_RAM),
                new MemoryReadAddress(0x2000, 0x2003, pia_5_r),
                new MemoryReadAddress(0x4000, 0x4003, pia_4_r),
                new MemoryReadAddress(0xf000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress zoo_readmem_sound[]
            = {
                new MemoryReadAddress(0x0000, 0x007f, MRA_RAM),
                new MemoryReadAddress(0x2000, 0x2003, pia_5_r),
                new MemoryReadAddress(0x4000, 0x4003, pia_4_r),
                new MemoryReadAddress(0xd000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x8000, 0x83ff, qix_sharedram_w, qix_sharedram),
                new MemoryWriteAddress(0x8400, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0x8c00, 0x8c00, qix_video_firq_w),
                new MemoryWriteAddress(0x9000, 0x9003, pia_3_w),
                new MemoryWriteAddress(0x9400, 0x9403, sdungeon_pia_0_w),
                new MemoryWriteAddress(0x9900, 0x9903, pia_1_w),
                new MemoryWriteAddress(0x9c00, 0x9fff, pia_2_w),
                new MemoryWriteAddress(0xa000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress zoo_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, qix_sharedram_w, qix_sharedram),
                new MemoryWriteAddress(0x0400, 0x07ff, MWA_RAM),
                new MemoryWriteAddress(0x0c00, 0x0c00, qix_video_firq_w),
                new MemoryWriteAddress(0x0c01, 0x0c01, MWA_NOP), /* interrupt acknowledge */
                new MemoryWriteAddress(0x1000, 0x1003, pia_3_w), /* Sound PIA */
                new MemoryWriteAddress(0x1400, 0x1403, sdungeon_pia_0_w), /* Game PIA 1 */
                new MemoryWriteAddress(0x1900, 0x1903, pia_1_w), /* Game PIA 2 */
                new MemoryWriteAddress(0x1c00, 0x1fff, pia_2_w), /* Game PIA 3 */
                new MemoryWriteAddress(0x8000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_video[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, qix_videoram_w),
                new MemoryWriteAddress(0x8000, 0x83ff, qix_sharedram_w),
                new MemoryWriteAddress(0x8400, 0x87ff, MWA_RAM, nvram, nvram_size),
                new MemoryWriteAddress(0x8800, 0x8800, qix_palettebank_w, qix_palettebank),
                new MemoryWriteAddress(0x8c00, 0x8c00, qix_data_firq_w),
                new MemoryWriteAddress(0x9000, 0x93ff, qix_paletteram_w, paletteram),
                new MemoryWriteAddress(0x9400, 0x9400, qix_addresslatch_w),
                new MemoryWriteAddress(0x9402, 0x9403, MWA_RAM, qix_videoaddress),
                new MemoryWriteAddress(0x9c00, 0x9FFF, MWA_RAM), /* Video controller */
                new MemoryWriteAddress(0xa000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress zoo_writemem_video[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, qix_videoram_w),
                new MemoryWriteAddress(0x8000, 0x83ff, qix_sharedram_w),
                new MemoryWriteAddress(0x8400, 0x87ff, MWA_RAM, nvram, nvram_size), /////protected when coin door is closed
                new MemoryWriteAddress(0x8800, 0x8800, qix_palettebank_w, qix_palettebank), /* LEDs are upper 6 bits */
                new MemoryWriteAddress(0x8801, 0x8801, zoo_bankswitch_w),
                new MemoryWriteAddress(0x8c00, 0x8c00, qix_data_firq_w),
                new MemoryWriteAddress(0x8c01, 0x8c01, MWA_NOP), /* interrupt acknowledge */
                new MemoryWriteAddress(0x9000, 0x93ff, qix_paletteram_w, paletteram),
                new MemoryWriteAddress(0x9400, 0x9400, qix_addresslatch_w),
                new MemoryWriteAddress(0x9402, 0x9403, MWA_RAM, qix_videoaddress),
                new MemoryWriteAddress(0xa000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_sound[]
            = {
                new MemoryWriteAddress(0x0000, 0x007f, MWA_RAM),
                new MemoryWriteAddress(0x2000, 0x2003, pia_5_w),
                new MemoryWriteAddress(0x4000, 0x4003, pia_4_w),
                new MemoryWriteAddress(0xf000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress zoo_writemem_sound[]
            = {
                new MemoryWriteAddress(0x0000, 0x007f, MWA_RAM),
                new MemoryWriteAddress(0x2000, 0x2003, pia_5_w),
                new MemoryWriteAddress(0x4000, 0x4003, pia_4_w),
                new MemoryWriteAddress(0xd000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress mcu_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0000, sdungeon_68705_portA_r),
                new MemoryReadAddress(0x0001, 0x0001, sdungeon_68705_portB_r),
                new MemoryReadAddress(0x0002, 0x0002, sdungeon_68705_portC_r),
                new MemoryReadAddress(0x0010, 0x007f, MRA_RAM),
                new MemoryReadAddress(0x0080, 0x07ff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress mcu_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0000, sdungeon_68705_portA_w),
                new MemoryWriteAddress(0x0001, 0x0001, sdungeon_68705_portB_w),
                new MemoryWriteAddress(0x0002, 0x0002, sdungeon_68705_portC_w),
                new MemoryWriteAddress(0x0004, 0x0004, sdungeon_68705_ddrA_w),
                new MemoryWriteAddress(0x0005, 0x0005, sdungeon_68705_ddrB_w),
                new MemoryWriteAddress(0x0006, 0x0006, sdungeon_68705_ddrC_w),
                new MemoryWriteAddress(0x0010, 0x007f, MWA_RAM),
                new MemoryWriteAddress(0x0080, 0x07ff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_qix = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* PIA 0 Port A (PLAYER 1) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1);

            PORT_START();
            /* PIA 0 Port B (COIN) */

            PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_SERVICE, "Test Advance", KEYCODE_F1, IP_JOY_DEFAULT);
            PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_SERVICE, "Test Next line", KEYCODE_F2, IP_JOY_DEFAULT);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, "Test Slew Up", KEYCODE_F5, IP_JOY_DEFAULT);
            PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_SERVICE, "Test Slew Down", KEYCODE_F6, IP_JOY_DEFAULT);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_TILT);

            PORT_START();
            /* PIA 1 Port A (SPARE) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PIA 1 Port B (PLAYER 1/2) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PIA 2 Port A (PLAYER 2) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_sdungeon = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* PIA 0 Port A (PLAYER 1) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_8WAY);

            PORT_START();
            /* PIA 0 Port B (COIN) */

            PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_SERVICE, "Test Advance", KEYCODE_F1, IP_JOY_DEFAULT);
            PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_SERVICE, "Test Next line", KEYCODE_F2, IP_JOY_DEFAULT);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, "Test Slew Up", KEYCODE_F5, IP_JOY_DEFAULT);
            PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_SERVICE, "Test Slew Down", KEYCODE_F6, IP_JOY_DEFAULT);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_TILT);

            PORT_START();
            /* PIA 1 Port A (SPARE) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PIA 1 Port B (PLAYER 1/2) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PIA 2 Port A (PLAYER 2) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_8WAY | IPF_COCKTAIL);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_elecyoyo = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* PIA 0 Port A (PLAYER 1) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PIA 0 Port B (COIN) */

            PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_SERVICE, "Test Advance", KEYCODE_F1, IP_JOY_DEFAULT);
            PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_SERVICE, "Test Next line", KEYCODE_F2, IP_JOY_DEFAULT);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, "Test Slew Up", KEYCODE_F5, IP_JOY_DEFAULT);
            PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_SERVICE, "Test Slew Down", KEYCODE_F6, IP_JOY_DEFAULT);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_TILT);

            PORT_START();
            /* PIA 1 Port A (SPARE) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PIA 1 Port B (PLAYER 1/2) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PIA 2 Port A (PLAYER 2) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_kram = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* PIA 0 Port A (PLAYER 1) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1);

            PORT_START();
            /* PIA 0 Port B (COIN) */

            PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_SERVICE, "Test Advance", KEYCODE_F1, IP_JOY_DEFAULT);
            PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_SERVICE, "Test Next line", KEYCODE_F2, IP_JOY_DEFAULT);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, "Test Slew Up", KEYCODE_F5, IP_JOY_DEFAULT);
            PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_SERVICE, "Test Slew Down", KEYCODE_F6, IP_JOY_DEFAULT);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_TILT);

            PORT_START();
            /* PIA 1 Port A (SPARE) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PIA 1 Port B (PLAYER 1/2) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PIA 2 Port A (PLAYER 2) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_zookeep = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* PIA 0 Port A (PLAYER 1) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PIA 0 Port B (COIN) */

            PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_SERVICE, "Test Advance", KEYCODE_F1, IP_JOY_DEFAULT);
            PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_SERVICE, "Test Next line", KEYCODE_F2, IP_JOY_DEFAULT);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, "Test Slew Up", KEYCODE_F5, IP_JOY_DEFAULT);
            PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_SERVICE, "Test Slew Down", KEYCODE_F6, IP_JOY_DEFAULT);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_TILT);

            PORT_START();
            /* PIA 1 Port A (SPARE) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PIA 1 Port B (PLAYER 1/2) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PIA 2 Port A (PLAYER 2) */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static DACinterface dac_interface = new DACinterface(
            1,
            new int[]{100}
    );

    static MachineDriver machine_driver_qix = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        1250000, /* 1.25 MHz */
                        readmem, writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_M6809,
                        1250000, /* 1.25 MHz */
                        readmem_video, writemem_video, null, null,
                        ignore_interrupt, 0
                ),
                new MachineCPU(
                        CPU_M6802 | CPU_AUDIO_CPU,
                        3680000 / 4, /* 0.92 MHz */
                        readmem_sound, writemem_sound, null, null,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            60, /* 60 CPU slices per frame - an high value to ensure proper */
            /* synchronization of the CPUs */
            qix_init_machine, /* init machine routine */ /* JB 970526 */
            /* video hardware */
            256, 256, /* screen_width, screen_height */
            new rectangle(0, 255, 8, 247), /* struct rectangle visible_area - just a guess */
            null, /* GfxDecodeInfo * */
            256, /* total colors */
            0, /* color table length */
            null, /* convert color prom routine */
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_SUPPORTS_DIRTY,
            null, /* vh_init routine */
            qix_vh_start, /* vh_start routine */
            qix_vh_stop, /* vh_stop routine */
            qix_vh_screenrefresh, /* vh_update routine */
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_DAC,
                        dac_interface
                )
            },
            nvram_handler
    );

    static MachineDriver machine_driver_mcu = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        1250000, /* 1.25 MHz */
                        readmem, writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_M6809,
                        1250000, /* 1.25 MHz */
                        readmem_video, writemem_video, null, null,
                        ignore_interrupt, 0
                ),
                new MachineCPU(
                        CPU_M6802 | CPU_AUDIO_CPU,
                        3680000 / 4, /* 0.92 MHz */
                        readmem_sound, writemem_sound, null, null,
                        ignore_interrupt, 0
                ),
                new MachineCPU(
                        CPU_M68705,
                        4000000 / 2, /* xtal is 4MHz, I think it's divided by 2 internally */
                        mcu_readmem, mcu_writemem, null, null,
                        ignore_interrupt, 0 /* No periodic interrupt */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            600, /* 60 CPU slices per frame - an high value to ensure proper */
            /* synchronization of the CPUs */
            withmcu_init_machine, /* init machine routine */ /* JB 970526 */
            /* video hardware */
            256, 256, /* screen_width, screen_height */
            new rectangle(0, 255, 8, 247), /* struct rectangle visible_area - just a guess */
            null, /* GfxDecodeInfo * */
            256, /* total colors */
            0, /* color table length */
            null, /* convert color prom routine */
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_SUPPORTS_DIRTY,
            null, /* vh_init routine */
            qix_vh_start, /* vh_start routine */
            qix_vh_stop, /* vh_stop routine */
            qix_vh_screenrefresh, /* vh_update routine */
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_DAC,
                        dac_interface
                )
            },
            nvram_handler
    );

    static MachineDriver machine_driver_zookeep = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        1250000, /* 1.25 MHz */
                        zoo_readmem, zoo_writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_M6809,
                        1250000, /* 1.25 MHz */
                        zoo_readmem_video, zoo_writemem_video, null, null,
                        ignore_interrupt, 0
                ),
                new MachineCPU(
                        CPU_M6802 | CPU_AUDIO_CPU,
                        3680000 / 4, /* 0.92 MHz */
                        zoo_readmem_sound, zoo_writemem_sound, null, null,
                        ignore_interrupt, 0
                ),
                new MachineCPU(
                        CPU_M68705,
                        4000000 / 2, /* xtal is 4MHz, I think it's divided by 2 internally */
                        mcu_readmem, mcu_writemem, null, null,
                        ignore_interrupt, 0 /* No periodic interrupt */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            60, /* 60 CPU slices per frame - an high value to ensure proper */
            /* synchronization of the CPUs */
            zoo_init_machine, /* init machine routine */
            /* video hardware */
            256, 256, /* screen_width, screen_height */
            new rectangle(0, 255, 8, 247), /* struct rectangle visible_area - just a guess */
            null, /* GfxDecodeInfo * */
            256, /* total colors */
            0, /* color table length */
            null, /* convert color prom routine */
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_SUPPORTS_DIRTY,
            null, /* vh_init routine */
            qix_vh_start, /* vh_start routine */
            qix_vh_stop, /* vh_stop routine */
            qix_vh_screenrefresh, /* vh_update routine */
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_DAC,
                        dac_interface
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
    static RomLoadHandlerPtr rom_qix = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU (Data) */

            ROM_LOAD("u12", 0xC000, 0x0800, 0xaad35508);
            ROM_LOAD("u13", 0xC800, 0x0800, 0x46c13504);
            ROM_LOAD("u14", 0xD000, 0x0800, 0x5115e896);
            ROM_LOAD("u15", 0xD800, 0x0800, 0xccd52a1b);
            ROM_LOAD("u16", 0xE000, 0x0800, 0xcd1c36ee);
            ROM_LOAD("u17", 0xE800, 0x0800, 0x1acb682d);
            ROM_LOAD("u18", 0xF000, 0x0800, 0xde77728b);
            ROM_LOAD("u19", 0xF800, 0x0800, 0xc0994776);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for code for the second CPU (Video) */

            ROM_LOAD("u4", 0xC800, 0x0800, 0x5b906a09);
            ROM_LOAD("u5", 0xD000, 0x0800, 0x254a3587);
            ROM_LOAD("u6", 0xD800, 0x0800, 0xace30389);
            ROM_LOAD("u7", 0xE000, 0x0800, 0x8ebcfa7c);
            ROM_LOAD("u8", 0xE800, 0x0800, 0xb8a3c8f9);
            ROM_LOAD("u9", 0xF000, 0x0800, 0x26cbcd55);
            ROM_LOAD("u10", 0xF800, 0x0800, 0x568be942);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for code for the third CPU (sound) */

            ROM_LOAD("u27", 0xF800, 0x0800, 0xf3782bd0);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_qixa = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU (Data) */

            ROM_LOAD("qu12", 0xC000, 0x0800, 0x1c55b44d);
            ROM_LOAD("qu13", 0xC800, 0x0800, 0x20279e8c);
            ROM_LOAD("qu14", 0xD000, 0x0800, 0xbafe3ce3);
            /* d800-dfff empty */
            ROM_LOAD("qu16", 0xE000, 0x0800, 0xdb560753);
            ROM_LOAD("qu17", 0xE800, 0x0800, 0x8c7aeed8);
            ROM_LOAD("qu18", 0xF000, 0x0800, 0x353be980);
            ROM_LOAD("qu19", 0xF800, 0x0800, 0xf46a69ca);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for code for the second CPU (Video) */

            ROM_LOAD("qu3", 0xC000, 0x0800, 0x8b4c0ef0);
            ROM_LOAD("qu4", 0xC800, 0x0800, 0x66a5c260);
            ROM_LOAD("qu5", 0xD000, 0x0800, 0x70160ea3);
            /* d800-dfff empty */
            ROM_LOAD("qu7", 0xE000, 0x0800, 0xd6733019);
            ROM_LOAD("qu8", 0xE800, 0x0800, 0x66870dcc);
            ROM_LOAD("qu9", 0xF000, 0x0800, 0xc99bf94d);
            ROM_LOAD("qu10", 0xF800, 0x0800, 0x88b45037);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for code for the third CPU (sound) */

            ROM_LOAD("u27", 0xF800, 0x0800, 0xf3782bd0);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_qixb = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU (Data) */

            ROM_LOAD("lk14.bin", 0xC000, 0x1000, 0x6d164986);
            ROM_LOAD("lk15.bin", 0xD000, 0x1000, 0x16c6ce0f);
            ROM_LOAD("lk16.bin", 0xE000, 0x1000, 0x698b1f9c);
            ROM_LOAD("lk17.bin", 0xF000, 0x1000, 0x7e3adde6);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for code for the second CPU (Video) */

            ROM_LOAD("lk10.bin", 0xC000, 0x1000, 0x7eac67d0);
            ROM_LOAD("lk11.bin", 0xD000, 0x1000, 0x90ccbb6a);
            ROM_LOAD("lk12.bin", 0xE000, 0x1000, 0xbe9b9f7d);
            ROM_LOAD("lk13.bin", 0xF000, 0x1000, 0x51c9853b);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for code for the third CPU (sound) */

            ROM_LOAD("u27", 0xF800, 0x0800, 0xf3782bd0);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_qix2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU (Data) */

            ROM_LOAD("u12.rmb", 0xC000, 0x0800, 0x484280fd);
            ROM_LOAD("u13.rmb", 0xC800, 0x0800, 0x3d089fcb);
            ROM_LOAD("u14.rmb", 0xD000, 0x0800, 0x362123a9);
            ROM_LOAD("u15.rmb", 0xD800, 0x0800, 0x60f3913d);
            ROM_LOAD("u16.rmb", 0xE000, 0x0800, 0xcc139e34);
            ROM_LOAD("u17.rmb", 0xE800, 0x0800, 0xcf31dc49);
            ROM_LOAD("u18.rmb", 0xF000, 0x0800, 0x1f91ed7a);
            ROM_LOAD("u19.rmb", 0xF800, 0x0800, 0x68e8d5a6);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for code for the second CPU (Video) */

            ROM_LOAD("u3.rmb", 0xC000, 0x0800, 0x19cebaca);
            ROM_LOAD("u4.rmb", 0xC800, 0x0800, 0x6cfb4185);
            ROM_LOAD("u5.rmb", 0xD000, 0x0800, 0x948f53f3);
            ROM_LOAD("u6.rmb", 0xD800, 0x0800, 0x8630120e);
            ROM_LOAD("u7.rmb", 0xE000, 0x0800, 0xbad037c9);
            ROM_LOAD("u8.rmb", 0xE800, 0x0800, 0x3159bc00);
            ROM_LOAD("u9.rmb", 0xF000, 0x0800, 0xe80e9b1d);
            ROM_LOAD("u10.rmb", 0xF800, 0x0800, 0x9a55d360);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for code for the third CPU (sound) */

            ROM_LOAD("u27", 0xF800, 0x0800, 0xf3782bd0);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_sdungeon = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU (Data) */

            ROM_LOAD("sd14.u14", 0xA000, 0x1000, 0x7024b55a);
            ROM_LOAD("sd15.u15", 0xB000, 0x1000, 0xa3ac9040);
            ROM_LOAD("sd16.u16", 0xC000, 0x1000, 0xcc20b580);
            ROM_LOAD("sd17.u17", 0xD000, 0x1000, 0x4663e4b8);
            ROM_LOAD("sd18.u18", 0xE000, 0x1000, 0x7ef1ffc0);
            ROM_LOAD("sd19.u19", 0xF000, 0x1000, 0x7b20b7ac);

            ROM_REGION(0x12000, REGION_CPU2);
            /* 64k for code + 2 ROM banks for the second CPU (Video) */

            ROM_LOAD("sd05.u5", 0x0A000, 0x1000, 0x0b2bf48e);
            ROM_LOAD("sd06.u6", 0x0B000, 0x1000, 0xf86db512);
            ROM_LOAD("sd07.u7", 0x0C000, 0x1000, 0x7b796831);
            ROM_LOAD("sd08.u8", 0x0D000, 0x1000, 0x5fbe7068);
            ROM_LOAD("sd09.u9", 0x0E000, 0x1000, 0x89bc51ea);
            ROM_LOAD("sd10.u10", 0x0F000, 0x1000, 0x754de734);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for code for the third CPU (sound) */

            ROM_LOAD("sd26.u26", 0xF000, 0x0800, 0x3df8630d);
            ROM_LOAD("sd27.u27", 0xF800, 0x0800, 0x0386f351);

            ROM_REGION(0x0800, REGION_CPU4);/* 2k for the 68705 microcontroller */

            ROM_LOAD("sd101", 0x0000, 0x0800, 0xe255af9a);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_elecyoyo = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU (Data) */

            ROM_LOAD("yy14", 0xA000, 0x1000, 0x0d2edcb9);
            ROM_LOAD("yy15", 0xB000, 0x1000, 0xa91f01e3);
            ROM_LOAD("yy16-1", 0xC000, 0x1000, 0x2710f360);
            ROM_LOAD("yy17", 0xD000, 0x1000, 0x25fd489d);
            ROM_LOAD("yy18", 0xE000, 0x1000, 0x0b6661c0);
            ROM_LOAD("yy19-1", 0xF000, 0x1000, 0x95b8b244);

            ROM_REGION(0x12000, REGION_CPU2);
            /* 64k for code + 2 ROM banks for the second CPU (Video) */

            ROM_LOAD("yy5", 0x0A000, 0x1000, 0x3793fec5);
            ROM_LOAD("yy6", 0x0B000, 0x1000, 0x2e8b1265);
            ROM_LOAD("yy7", 0x0C000, 0x1000, 0x20f93411);
            ROM_LOAD("yy8", 0x0D000, 0x1000, 0x926f90c8);
            ROM_LOAD("yy9", 0x0E000, 0x1000, 0x2f999480);
            ROM_LOAD("yy10", 0x0F000, 0x1000, 0xb31d20e2);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for code for the third CPU (sound) */

            ROM_LOAD("yy27", 0xF800, 0x0800, 0x5a2aa0f3);

            ROM_REGION(0x0800, REGION_CPU4);/* 2k for the 68705 microcontroller */

            ROM_LOAD("yy101", 0x0000, 0x0800, 0x3cf13038);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_elecyoy2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU (Data) */

            ROM_LOAD("yy14", 0xA000, 0x1000, 0x0d2edcb9);
            ROM_LOAD("yy15", 0xB000, 0x1000, 0xa91f01e3);
            ROM_LOAD("yy16", 0xC000, 0x1000, 0xcab19f3a);
            ROM_LOAD("yy17", 0xD000, 0x1000, 0x25fd489d);
            ROM_LOAD("yy18", 0xE000, 0x1000, 0x0b6661c0);
            ROM_LOAD("yy19", 0xF000, 0x1000, 0xd0215d2e);

            ROM_REGION(0x12000, REGION_CPU2);
            /* 64k for code + 2 ROM banks for the second CPU (Video) */

            ROM_LOAD("yy5", 0x0A000, 0x1000, 0x3793fec5);
            ROM_LOAD("yy6", 0x0B000, 0x1000, 0x2e8b1265);
            ROM_LOAD("yy7", 0x0C000, 0x1000, 0x20f93411);
            ROM_LOAD("yy8", 0x0D000, 0x1000, 0x926f90c8);
            ROM_LOAD("yy9", 0x0E000, 0x1000, 0x2f999480);
            ROM_LOAD("yy10", 0x0F000, 0x1000, 0xb31d20e2);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for code for the third CPU (sound) */

            ROM_LOAD("yy27", 0xF800, 0x0800, 0x5a2aa0f3);

            ROM_REGION(0x0800, REGION_CPU4);/* 2k for the 68705 microcontroller */

            ROM_LOAD("yy101", 0x0000, 0x0800, 0x3cf13038);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_kram = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU (Data) */

            ROM_LOAD("ks14-1", 0xA000, 0x1000, 0xfe69ac79);
            ROM_LOAD("ks15", 0xB000, 0x1000, 0x4b2c175e);
            ROM_LOAD("ks16", 0xC000, 0x1000, 0x9500a05d);
            ROM_LOAD("ks17", 0xD000, 0x1000, 0xc752a3a1);
            ROM_LOAD("ks18", 0xE000, 0x1000, 0x79158b03);
            ROM_LOAD("ks19-1", 0xF000, 0x1000, 0x759ea6ce);

            ROM_REGION(0x12000, REGION_CPU2);
            /* 64k for code + 2 ROM banks for the second CPU (Video) */

            ROM_LOAD("ks5", 0x0A000, 0x1000, 0x1c472080);
            ROM_LOAD("ks6", 0x0B000, 0x1000, 0xb8926622);
            ROM_LOAD("ks7", 0x0C000, 0x1000, 0xc98a7485);
            ROM_LOAD("ks8", 0x0D000, 0x1000, 0x1127c4e4);
            ROM_LOAD("ks9", 0x0E000, 0x1000, 0xd3bc8b5e);
            ROM_LOAD("ks10", 0x0F000, 0x1000, 0xe0426444);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for code for the third CPU (sound) */

            ROM_LOAD("ks27", 0xf800, 0x0800, 0xc46530c8);

            ROM_REGION(0x0800, REGION_CPU4);/* 2k for the 68705 microcontroller */

            ROM_LOAD("ks101.dat", 0x0000, 0x0800, 0xe53d97b7);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_kram2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU (Data) */

            ROM_LOAD("ks14", 0xA000, 0x1000, 0xa2eac1ff);
            ROM_LOAD("ks15", 0xB000, 0x1000, 0x4b2c175e);
            ROM_LOAD("ks16", 0xC000, 0x1000, 0x9500a05d);
            ROM_LOAD("ks17", 0xD000, 0x1000, 0xc752a3a1);
            ROM_LOAD("ks18", 0xE000, 0x1000, 0x79158b03);
            ROM_LOAD("ks19", 0xF000, 0x1000, 0x053c5e09);

            ROM_REGION(0x12000, REGION_CPU2);
            /* 64k for code + 2 ROM banks for the second CPU (Video) */

            ROM_LOAD("ks5", 0x0A000, 0x1000, 0x1c472080);
            ROM_LOAD("ks6", 0x0B000, 0x1000, 0xb8926622);
            ROM_LOAD("ks7", 0x0C000, 0x1000, 0xc98a7485);
            ROM_LOAD("ks8", 0x0D000, 0x1000, 0x1127c4e4);
            ROM_LOAD("ks9", 0x0E000, 0x1000, 0xd3bc8b5e);
            ROM_LOAD("ks10", 0x0F000, 0x1000, 0xe0426444);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for code for the third CPU (sound) */

            ROM_LOAD("ks27", 0xf800, 0x0800, 0xc46530c8);

            ROM_REGION(0x0800, REGION_CPU4);/* 2k for the 68705 microcontroller */

            ROM_LOAD("ks101.dat", 0x0000, 0x0800, 0xe53d97b7);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_zookeep = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU (Data) */

            ROM_LOAD("za12", 0x8000, 0x1000, 0x4e40d8dc);
            ROM_LOAD("za13", 0x9000, 0x1000, 0xeebd5248);
            ROM_LOAD("za14", 0xA000, 0x1000, 0xfab43297);
            ROM_LOAD("za15", 0xB000, 0x1000, 0xef8cd67c);
            ROM_LOAD("za16", 0xC000, 0x1000, 0xccfc15bc);
            ROM_LOAD("za17", 0xD000, 0x1000, 0x358013f4);
            ROM_LOAD("za18", 0xE000, 0x1000, 0x37886afe);
            ROM_LOAD("za19", 0xF000, 0x1000, 0xbbfb30d9);

            ROM_REGION(0x12000, REGION_CPU2);
            /* 64k for code + 2 ROM banks for the second CPU (Video) */

            ROM_LOAD("za5", 0x0A000, 0x1000, 0xdc0c3cbd);
            ROM_LOAD("za3", 0x10000, 0x1000, 0xcc4d0aee);
            ROM_LOAD("za6", 0x0B000, 0x1000, 0x27c787dd);
            ROM_LOAD("za4", 0x11000, 0x1000, 0xec3b10b1);

            ROM_LOAD("za7", 0x0C000, 0x1000, 0x1479f480);
            ROM_LOAD("za8", 0x0D000, 0x1000, 0x4c96cdb2);
            ROM_LOAD("za9", 0x0E000, 0x1000, 0xa4f7d9e0);
            ROM_LOAD("za10", 0x0F000, 0x1000, 0x05df1a5a);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for code for the third CPU (sound) */

            ROM_LOAD("za25", 0xD000, 0x1000, 0x779b8558);
            ROM_LOAD("za26", 0xE000, 0x1000, 0x60a810ce);
            ROM_LOAD("za27", 0xF000, 0x1000, 0x99ed424e);

            ROM_REGION(0x0800, REGION_CPU4);/* 2k for the 68705 microcontroller */

            ROM_LOAD("za_coin.bin", 0x0000, 0x0800, 0x364d3557);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_zookeep2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU (Data) */

            ROM_LOAD("za12", 0x8000, 0x1000, 0x4e40d8dc);
            ROM_LOAD("za13", 0x9000, 0x1000, 0xeebd5248);
            ROM_LOAD("za14", 0xA000, 0x1000, 0xfab43297);
            ROM_LOAD("za15", 0xB000, 0x1000, 0xef8cd67c);
            ROM_LOAD("za16", 0xC000, 0x1000, 0xccfc15bc);
            ROM_LOAD("za17", 0xD000, 0x1000, 0x358013f4);
            ROM_LOAD("za18", 0xE000, 0x1000, 0x37886afe);
            ROM_LOAD("za19.red", 0xF000, 0x1000, 0xec01760e);

            ROM_REGION(0x12000, REGION_CPU2);
            /* 64k for code + 2 ROM banks for the second CPU (Video) */

            ROM_LOAD("za5", 0x0A000, 0x1000, 0xdc0c3cbd);
            ROM_LOAD("za3", 0x10000, 0x1000, 0xcc4d0aee);
            ROM_LOAD("za6", 0x0B000, 0x1000, 0x27c787dd);
            ROM_LOAD("za4", 0x11000, 0x1000, 0xec3b10b1);

            ROM_LOAD("za7", 0x0C000, 0x1000, 0x1479f480);
            ROM_LOAD("za8", 0x0D000, 0x1000, 0x4c96cdb2);
            ROM_LOAD("za9", 0x0E000, 0x1000, 0xa4f7d9e0);
            ROM_LOAD("za10", 0x0F000, 0x1000, 0x05df1a5a);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for code for the third CPU (sound) */

            ROM_LOAD("za25", 0xD000, 0x1000, 0x779b8558);
            ROM_LOAD("za26", 0xE000, 0x1000, 0x60a810ce);
            ROM_LOAD("za27", 0xF000, 0x1000, 0x99ed424e);

            ROM_REGION(0x0800, REGION_CPU4);/* 2k for the 68705 microcontroller */

            ROM_LOAD("za_coin.bin", 0x0000, 0x0800, 0x364d3557);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_zookeep3 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU (Data) */

            ROM_LOAD("za12", 0x8000, 0x1000, 0x4e40d8dc);
            ROM_LOAD("za13", 0x9000, 0x1000, 0xeebd5248);
            ROM_LOAD("za14", 0xA000, 0x1000, 0xfab43297);
            ROM_LOAD("za15", 0xB000, 0x1000, 0xef8cd67c);
            ROM_LOAD("za16", 0xC000, 0x1000, 0xccfc15bc);
            ROM_LOAD("za17", 0xD000, 0x1000, 0x358013f4);
            ROM_LOAD("za18", 0xE000, 0x1000, 0x37886afe);
            ROM_LOAD("za19", 0xF000, 0x1000, 0xbbfb30d9);

            ROM_REGION(0x12000, REGION_CPU2);
            /* 64k for code + 2 ROM banks for the second CPU (Video) */

            ROM_LOAD("za5", 0x0A000, 0x1000, 0xdc0c3cbd);
            ROM_LOAD("za3", 0x10000, 0x1000, 0xcc4d0aee);
            ROM_LOAD("za6", 0x0B000, 0x1000, 0x27c787dd);
            ROM_LOAD("za4", 0x11000, 0x1000, 0xec3b10b1);

            ROM_LOAD("za7", 0x0C000, 0x1000, 0x1479f480);
            ROM_LOAD("za8", 0x0D000, 0x1000, 0x4c96cdb2);
            ROM_LOAD("zv35.9", 0x0E000, 0x1000, 0xd14123b7);
            ROM_LOAD("zv36.10", 0x0F000, 0x1000, 0x23705777);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for code for the third CPU (sound) */

            ROM_LOAD("za25", 0xD000, 0x1000, 0x779b8558);
            ROM_LOAD("za26", 0xE000, 0x1000, 0x60a810ce);
            ROM_LOAD("za27", 0xF000, 0x1000, 0x99ed424e);

            ROM_REGION(0x0800, REGION_CPU4);/* 2k for the 68705 microcontroller */

            ROM_LOAD("za_coin.bin", 0x0000, 0x0800, 0x364d3557);
            ROM_END();
        }
    };

    public static GameDriver driver_qix = new GameDriver("1981", "qix", "qix.java", rom_qix, null, machine_driver_qix, input_ports_qix, null, ROT270, "Taito America Corporation", "Qix (set 1)", GAME_NO_COCKTAIL);
    public static GameDriver driver_qixa = new GameDriver("1981", "qixa", "qix.java", rom_qixa, driver_qix, machine_driver_qix, input_ports_qix, null, ROT270, "Taito America Corporation", "Qix (set 2)", GAME_NO_COCKTAIL);
    public static GameDriver driver_qixb = new GameDriver("1981", "qixb", "qix.java", rom_qixb, driver_qix, machine_driver_qix, input_ports_qix, null, ROT270, "Taito America Corporation", "Qix (set 3)", GAME_NO_COCKTAIL);
    public static GameDriver driver_qix2 = new GameDriver("1981", "qix2", "qix.java", rom_qix2, driver_qix, machine_driver_qix, input_ports_qix, null, ROT270, "Taito America Corporation", "Qix II (Tournament)", GAME_NO_COCKTAIL);
    public static GameDriver driver_sdungeon = new GameDriver("1981", "sdungeon", "qix.java", rom_sdungeon, null, machine_driver_mcu, input_ports_sdungeon, null, ROT270, "Taito America Corporation", "Space Dungeon", GAME_NO_COCKTAIL);
    public static GameDriver driver_elecyoyo = new GameDriver("1982", "elecyoyo", "qix.java", rom_elecyoyo, null, machine_driver_mcu, input_ports_elecyoyo, null, ROT270, "Taito America Corporation", "The Electric Yo-Yo (set 1)", GAME_NO_COCKTAIL);
    public static GameDriver driver_elecyoy2 = new GameDriver("1982", "elecyoy2", "qix.java", rom_elecyoy2, driver_elecyoyo, machine_driver_mcu, input_ports_elecyoyo, null, ROT270, "Taito America Corporation", "The Electric Yo-Yo (set 2)", GAME_NO_COCKTAIL);
    public static GameDriver driver_kram = new GameDriver("1982", "kram", "qix.java", rom_kram, null, machine_driver_mcu, input_ports_kram, null, ROT0, "Taito America Corporation", "Kram (set 1)", GAME_NO_COCKTAIL);
    public static GameDriver driver_kram2 = new GameDriver("1982", "kram2", "qix.java", rom_kram2, driver_kram, machine_driver_mcu, input_ports_kram, null, ROT0, "Taito America Corporation", "Kram (set 2)", GAME_NO_COCKTAIL);
    public static GameDriver driver_zookeep = new GameDriver("1982", "zookeep", "qix.java", rom_zookeep, null, machine_driver_zookeep, input_ports_zookeep, null, ROT0, "Taito America Corporation", "Zoo Keeper (set 1)", GAME_NO_COCKTAIL);
    public static GameDriver driver_zookeep2 = new GameDriver("1982", "zookeep2", "qix.java", rom_zookeep2, driver_zookeep, machine_driver_zookeep, input_ports_zookeep, null, ROT0, "Taito America Corporation", "Zoo Keeper (set 2)", GAME_NO_COCKTAIL);
    public static GameDriver driver_zookeep3 = new GameDriver("1982", "zookeep3", "qix.java", rom_zookeep3, driver_zookeep, machine_driver_zookeep, input_ports_zookeep, null, ROT0, "Taito America Corporation", "Zoo Keeper (set 3)", GAME_NO_COCKTAIL);
}
