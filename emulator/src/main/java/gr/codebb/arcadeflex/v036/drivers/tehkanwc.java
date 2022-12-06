/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;

import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static arcadeflex.v036.vidhrdw.tehkanwc.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.MSM5205H.*;
import static gr.codebb.arcadeflex.v036.sound.MSM5205.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;

public class tehkanwc {

    static UBytePtr shared_ram = new UBytePtr();

    public static ReadHandlerPtr shared_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return shared_ram.read(offset);
        }
    };

    public static WriteHandlerPtr shared_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            shared_ram.write(offset, data);
        }
    };

    public static WriteHandlerPtr sub_cpu_halt_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (data != 0) {
                cpu_set_reset_line(1, CLEAR_LINE);
            } else {
                cpu_set_reset_line(1, ASSERT_LINE);
            }
        }
    };

    static int[] track0 = new int[2];
    static int[] track1 = new int[2];

    public static ReadHandlerPtr tehkanwc_track_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int joy;

            joy = readinputport(10) >> (2 * offset);
            if ((joy & 1) != 0) {
                return -63;
            }
            if ((joy & 2) != 0) {
                return 63;
            }
            return readinputport(3 + offset) - track0[offset];
        }
    };

    public static ReadHandlerPtr tehkanwc_track_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int joy;

            joy = readinputport(10) >> (4 + 2 * offset);
            if ((joy & 1) != 0) {
                return -63;
            }
            if ((joy & 2) != 0) {
                return 63;
            }
            return readinputport(6 + offset) - track1[offset];
        }
    };

    public static WriteHandlerPtr tehkanwc_track_0_reset_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* reset the trackball counters */
            track0[offset] = readinputport(3 + offset) + data;
        }
    };

    public static WriteHandlerPtr tehkanwc_track_1_reset_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* reset the trackball counters */
            track1[offset] = readinputport(6 + offset) + data;
        }
    };

    public static WriteHandlerPtr sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(offset, data);
            cpu_cause_interrupt(2, Z80_NMI_INT);
        }
    };
    public static TimerCallbackHandlerPtr reset_callback = new TimerCallbackHandlerPtr() {
        public void handler(int trigger) {

            cpu_set_reset_line(2, PULSE_LINE);
        }
    };

    public static WriteHandlerPtr sound_answer_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch2_w.handler(0, data);

            /* in Gridiron, the sound CPU goes in a tight loop after the self test, */
            /* probably waiting to be reset by a watchdog */
            if (cpu_get_pc() == 0x08bc) {
                timer_set(TIME_IN_SEC(1), 0, reset_callback);
            }
        }
    };

    /* Emulate MSM sound samples with counters */
    static int msm_data_offs;

    public static ReadHandlerPtr tehkanwc_portA_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return msm_data_offs & 0xff;
        }
    };

    public static ReadHandlerPtr tehkanwc_portB_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (msm_data_offs >> 8) & 0xff;
        }
    };

    public static WriteHandlerPtr tehkanwc_portA_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            msm_data_offs = (msm_data_offs & 0xff00) | data;
        }
    };

    public static WriteHandlerPtr tehkanwc_portB_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            msm_data_offs = (msm_data_offs & 0x00ff) | (data << 8);
        }
    };

    public static WriteHandlerPtr msm_reset_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            MSM5205_reset_w.handler(0, data != 0 ? 0 : 1);
        }
    };
    static int toggle;
    public static vclk_interruptPtr tehkanwc_adpcm_int = new vclk_interruptPtr() {
        public void handler(int data) {
            UBytePtr SAMPLES = memory_region(REGION_SOUND1);
            int msm_data = SAMPLES.read(msm_data_offs & 0x7fff);

            if (toggle == 0) {
                MSM5205_data_w.handler(0, (msm_data >> 4) & 0x0f);
            } else {
                MSM5205_data_w.handler(0, msm_data & 0x0f);
                msm_data_offs++;
            }

            toggle ^= 1;
        }
    };

    /* End of MSM with counters emulation */
    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xc7ff, MRA_RAM),
                new MemoryReadAddress(0xc800, 0xcfff, shared_r),
                new MemoryReadAddress(0xd000, 0xd3ff, videoram_r),
                new MemoryReadAddress(0xd400, 0xd7ff, colorram_r),
                new MemoryReadAddress(0xd800, 0xddff, paletteram_r),
                new MemoryReadAddress(0xde00, 0xdfff, MRA_RAM), /* unused part of the palette RAM, I think? Gridiron uses it */
                new MemoryReadAddress(0xe000, 0xe7ff, tehkanwc_videoram1_r),
                new MemoryReadAddress(0xe800, 0xebff, spriteram_r), /* sprites */
                new MemoryReadAddress(0xec00, 0xec01, tehkanwc_scroll_x_r),
                new MemoryReadAddress(0xec02, 0xec02, tehkanwc_scroll_y_r),
                new MemoryReadAddress(0xf800, 0xf801, tehkanwc_track_0_r), /* track 0 x/y */
                new MemoryReadAddress(0xf802, 0xf802, input_port_9_r), /* Coin  Start */
                new MemoryReadAddress(0xf803, 0xf803, input_port_5_r), /* joy0 - button */
                new MemoryReadAddress(0xf810, 0xf811, tehkanwc_track_1_r), /* track 1 x/y */
                new MemoryReadAddress(0xf813, 0xf813, input_port_8_r), /* joy1 - button */
                new MemoryReadAddress(0xf820, 0xf820, soundlatch2_r), /* answer from the sound CPU */
                new MemoryReadAddress(0xf840, 0xf840, input_port_0_r), /* DSW1 */
                new MemoryReadAddress(0xf850, 0xf850, input_port_1_r), /* DSW2 */
                new MemoryReadAddress(0xf860, 0xf860, watchdog_reset_r),
                new MemoryReadAddress(0xf870, 0xf870, input_port_2_r), /* DSW3 */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_RAM),
                new MemoryWriteAddress(0xc800, 0xcfff, shared_w, shared_ram),
                new MemoryWriteAddress(0xd000, 0xd3ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xd400, 0xd7ff, colorram_w, colorram),
                new MemoryWriteAddress(0xd800, 0xddff, paletteram_xxxxBBBBGGGGRRRR_swap_w, paletteram),
                new MemoryWriteAddress(0xde00, 0xdfff, MWA_RAM), /* unused part of the palette RAM, I think? Gridiron uses it */
                new MemoryWriteAddress(0xe000, 0xe7ff, tehkanwc_videoram1_w, tehkanwc_videoram1, tehkanwc_videoram1_size),
                new MemoryWriteAddress(0xe800, 0xebff, spriteram_w, spriteram, spriteram_size), /* sprites */
                new MemoryWriteAddress(0xec00, 0xec01, tehkanwc_scroll_x_w),
                new MemoryWriteAddress(0xec02, 0xec02, tehkanwc_scroll_y_w),
                new MemoryWriteAddress(0xf800, 0xf801, tehkanwc_track_0_reset_w),
                new MemoryWriteAddress(0xf802, 0xf802, gridiron_led0_w),
                new MemoryWriteAddress(0xf810, 0xf811, tehkanwc_track_1_reset_w),
                new MemoryWriteAddress(0xf812, 0xf812, gridiron_led1_w),
                new MemoryWriteAddress(0xf820, 0xf820, sound_command_w),
                new MemoryWriteAddress(0xf840, 0xf840, sub_cpu_halt_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress readmem_sub[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0xc7ff, MRA_RAM),
                new MemoryReadAddress(0xc800, 0xcfff, shared_r),
                new MemoryReadAddress(0xd000, 0xd3ff, videoram_r),
                new MemoryReadAddress(0xd400, 0xd7ff, colorram_r),
                new MemoryReadAddress(0xd800, 0xddff, paletteram_r),
                new MemoryReadAddress(0xde00, 0xdfff, MRA_RAM), /* unused part of the palette RAM, I think? Gridiron uses it */
                new MemoryReadAddress(0xe000, 0xe7ff, tehkanwc_videoram1_r),
                new MemoryReadAddress(0xe800, 0xebff, spriteram_r), /* sprites */
                new MemoryReadAddress(0xec00, 0xec01, tehkanwc_scroll_x_r),
                new MemoryReadAddress(0xec02, 0xec02, tehkanwc_scroll_y_r),
                new MemoryReadAddress(0xf860, 0xf860, watchdog_reset_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_sub[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_RAM),
                new MemoryWriteAddress(0xc800, 0xcfff, shared_w),
                new MemoryWriteAddress(0xd000, 0xd3ff, videoram_w),
                new MemoryWriteAddress(0xd400, 0xd7ff, colorram_w),
                new MemoryWriteAddress(0xd800, 0xddff, paletteram_xxxxBBBBGGGGRRRR_swap_w, paletteram),
                new MemoryWriteAddress(0xde00, 0xdfff, MWA_RAM), /* unused part of the palette RAM, I think? Gridiron uses it */
                new MemoryWriteAddress(0xe000, 0xe7ff, tehkanwc_videoram1_w),
                new MemoryWriteAddress(0xe800, 0xebff, spriteram_w), /* sprites */
                new MemoryWriteAddress(0xec00, 0xec01, tehkanwc_scroll_x_w),
                new MemoryWriteAddress(0xec02, 0xec02, tehkanwc_scroll_y_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress readmem_sound[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x47ff, MRA_RAM),
                new MemoryReadAddress(0xc000, 0xc000, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_sound[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x47ff, MWA_RAM),
                new MemoryWriteAddress(0x8001, 0x8001, msm_reset_w),/* MSM51xx reset */
                new MemoryWriteAddress(0x8002, 0x8002, MWA_NOP), /* ?? written in the IRQ handler */
                new MemoryWriteAddress(0x8003, 0x8003, MWA_NOP), /* ?? written in the NMI handler */
                new MemoryWriteAddress(0xc000, 0xc000, sound_answer_w), /* answer for main CPU */
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort sound_readport[]
            = {
                new IOReadPort(0x00, 0x00, AY8910_read_port_0_r),
                new IOReadPort(0x02, 0x02, AY8910_read_port_1_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort sound_writeport[]
            = {
                new IOWritePort(0x00, 0x00, AY8910_write_port_0_w),
                new IOWritePort(0x01, 0x01, AY8910_control_port_0_w),
                new IOWritePort(0x02, 0x02, AY8910_write_port_1_w),
                new IOWritePort(0x03, 0x03, AY8910_control_port_1_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_tehkanwc = new InputPortPtr() {
        public void handler() {
            PORT_START();  /* DSW1 - Active LOW */

            PORT_DIPNAME(0x07, 0x07, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x38, 0x38, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x38, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x28, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x20, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x18, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x40, 0x40, "Extra Time per Coin");
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x00, "Double");
            PORT_DIPNAME(0x80, 0x80, "1 Player Start");
            PORT_DIPSETTING(0x00, "2 Credits");
            PORT_DIPSETTING(0x80, "1 Credit");

            PORT_START();  /* DSW2 - Active LOW */

            PORT_DIPNAME(0x03, 0x03, "1P Game Time");
            PORT_DIPSETTING(0x00, "2:30");
            PORT_DIPSETTING(0x01, "2:00");
            PORT_DIPSETTING(0x03, "1:30");
            PORT_DIPSETTING(0x02, "1:00");
            PORT_DIPNAME(0x7c, 0x7c, "2P Game Time");
            PORT_DIPSETTING(0x00, "5:00/3:00 Extra");
            PORT_DIPSETTING(0x60, "5:00/2:45 Extra");
            PORT_DIPSETTING(0x20, "5:00/2:35 Extra");
            PORT_DIPSETTING(0x40, "5:00/2:30 Extra");
            PORT_DIPSETTING(0x04, "4:00/2:30 Extra");
            PORT_DIPSETTING(0x64, "4:00/2:15 Extra");
            PORT_DIPSETTING(0x24, "4:00/2:05 Extra");
            PORT_DIPSETTING(0x44, "4:00/2:00 Extra");
            PORT_DIPSETTING(0x1c, "3:30/2:15 Extra");
            PORT_DIPSETTING(0x7c, "3:30/2:00 Extra");
            PORT_DIPSETTING(0x3c, "3:30/1:50 Extra");
            PORT_DIPSETTING(0x5c, "3:30/1:45 Extra");
            PORT_DIPSETTING(0x08, "3:00/2:00 Extra");
            PORT_DIPSETTING(0x68, "3:00/1:45 Extra");
            PORT_DIPSETTING(0x28, "3:00/1:35 Extra");
            PORT_DIPSETTING(0x48, "3:00/1:30 Extra");
            PORT_DIPSETTING(0x0c, "2:30/1:45 Extra");
            PORT_DIPSETTING(0x6c, "2:30/1:30 Extra");
            PORT_DIPSETTING(0x2c, "2:30/1:20 Extra");
            PORT_DIPSETTING(0x4c, "2:30/1:15 Extra");
            PORT_DIPSETTING(0x10, "2:00/1:30 Extra");
            PORT_DIPSETTING(0x70, "2:00/1:15 Extra");
            PORT_DIPSETTING(0x30, "2:00/1:05 Extra");
            PORT_DIPSETTING(0x50, "2:00/1:00 Extra");
            PORT_DIPSETTING(0x14, "1:30/1:15 Extra");
            PORT_DIPSETTING(0x74, "1:30/1:00 Extra");
            PORT_DIPSETTING(0x34, "1:30/0:50 Extra");
            PORT_DIPSETTING(0x54, "1:30/0:45 Extra");
            PORT_DIPSETTING(0x18, "1:00/1:00 Extra");
            PORT_DIPSETTING(0x78, "1:00/0:45 Extra");
            PORT_DIPSETTING(0x38, "1:00/0:35 Extra");
            PORT_DIPSETTING(0x58, "1:00/0:30 Extra");
            PORT_DIPNAME(0x80, 0x80, "Game Type");
            PORT_DIPSETTING(0x80, "Timer In");
            PORT_DIPSETTING(0x00, "Credit In");

            PORT_START();  /* DSW3 - Active LOW */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x02, "Easy");
            PORT_DIPSETTING(0x03, "Normal");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Very Hard");
            PORT_DIPNAME(0x04, 0x04, "Timer Speed");
            PORT_DIPSETTING(0x04, "60/60");
            PORT_DIPSETTING(0x00, "55/60");
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));

            PORT_START();  /* IN0 - X AXIS */

            PORT_ANALOGX(0xff, 0x80, IPT_TRACKBALL_X | IPF_PLAYER1, 100, 0, 0, 0, IP_KEY_NONE, IP_KEY_NONE, IP_JOY_NONE, IP_JOY_NONE);

            PORT_START();  /* IN0 - Y AXIS */

            PORT_ANALOGX(0xff, 0x80, IPT_TRACKBALL_Y | IPF_PLAYER1, 100, 0, 0, 0, IP_KEY_NONE, IP_KEY_NONE, IP_JOY_NONE, IP_JOY_NONE);

            PORT_START();  /* IN0 - BUTTON */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);

            PORT_START();  /* IN1 - X AXIS */

            PORT_ANALOGX(0xff, 0x80, IPT_TRACKBALL_X | IPF_PLAYER2, 100, 0, 0, 0, IP_KEY_NONE, IP_KEY_NONE, IP_JOY_NONE, IP_JOY_NONE);

            PORT_START();  /* IN1 - Y AXIS */

            PORT_ANALOGX(0xff, 0x80, IPT_TRACKBALL_Y | IPF_PLAYER2, 100, 0, 0, 0, IP_KEY_NONE, IP_KEY_NONE, IP_JOY_NONE, IP_JOY_NONE);

            PORT_START();  /* IN1 - BUTTON */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);

            PORT_START();  /* IN2 - Active LOW */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);

            PORT_START(); 	/* fake port to emulate trackballs with keyboard */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_gridiron = new InputPortPtr() {
        public void handler() {
            PORT_START();  /* DSW1 - Active LOW */

            PORT_DIPNAME(0x01, 0x01, "1 Player Start");
            PORT_DIPSETTING(0x00, "2 Credits");
            PORT_DIPSETTING(0x01, "1 Credit");
            PORT_DIPNAME(0x02, 0x02, "2 Players Start");
            PORT_DIPSETTING(0x02, "2 Credits");
            PORT_DIPSETTING(0x00, "1 Credit");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();  /* DSW2 - Active LOW */

            PORT_DIPNAME(0x03, 0x03, "1P Game Time");
            PORT_DIPSETTING(0x00, "2:30");
            PORT_DIPSETTING(0x01, "2:00");
            PORT_DIPSETTING(0x03, "1:30");
            PORT_DIPSETTING(0x02, "1:00");
            PORT_DIPNAME(0x7c, 0x7c, "2P Game Time");
            PORT_DIPSETTING(0x60, "5:00/3:00 Extra");
            PORT_DIPSETTING(0x00, "5:00/2:45 Extra");
            PORT_DIPSETTING(0x20, "5:00/2:35 Extra");
            PORT_DIPSETTING(0x40, "5:00/2:30 Extra");
            PORT_DIPSETTING(0x64, "4:00/2:30 Extra");
            PORT_DIPSETTING(0x04, "4:00/2:15 Extra");
            PORT_DIPSETTING(0x24, "4:00/2:05 Extra");
            PORT_DIPSETTING(0x44, "4:00/2:00 Extra");
            PORT_DIPSETTING(0x68, "3:30/2:15 Extra");
            PORT_DIPSETTING(0x08, "3:30/2:00 Extra");
            PORT_DIPSETTING(0x28, "3:30/1:50 Extra");
            PORT_DIPSETTING(0x48, "3:30/1:45 Extra");
            PORT_DIPSETTING(0x6c, "3:00/2:00 Extra");
            PORT_DIPSETTING(0x0c, "3:00/1:45 Extra");
            PORT_DIPSETTING(0x2c, "3:00/1:35 Extra");
            PORT_DIPSETTING(0x4c, "3:00/1:30 Extra");
            PORT_DIPSETTING(0x7c, "2:30/1:45 Extra");
            PORT_DIPSETTING(0x1c, "2:30/1:30 Extra");
            PORT_DIPSETTING(0x3c, "2:30/1:20 Extra");
            PORT_DIPSETTING(0x5c, "2:30/1:15 Extra");
            PORT_DIPSETTING(0x70, "2:00/1:30 Extra");
            PORT_DIPSETTING(0x10, "2:00/1:15 Extra");
            PORT_DIPSETTING(0x30, "2:00/1:05 Extra");
            PORT_DIPSETTING(0x50, "2:00/1:00 Extra");
            PORT_DIPSETTING(0x74, "1:30/1:15 Extra");
            PORT_DIPSETTING(0x14, "1:30/1:00 Extra");
            PORT_DIPSETTING(0x34, "1:30/0:50 Extra");
            PORT_DIPSETTING(0x54, "1:30/0:45 Extra");
            PORT_DIPSETTING(0x78, "1:00/1:00 Extra");
            PORT_DIPSETTING(0x18, "1:00/0:45 Extra");
            PORT_DIPSETTING(0x38, "1:00/0:35 Extra");
            PORT_DIPSETTING(0x58, "1:00/0:30 Extra");
            PORT_DIPNAME(0x80, 0x80, "Game Type?");
            PORT_DIPSETTING(0x80, "Timer In");
            PORT_DIPSETTING(0x00, "Credit In");

            PORT_START();  /* DSW3 - Active LOW */

            PORT_DIPNAME(0x03, 0x03, "Difficulty?");
            PORT_DIPSETTING(0x02, "Easy");
            PORT_DIPSETTING(0x03, "Normal");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Very Hard");
            PORT_DIPNAME(0x04, 0x04, "Timer Speed?");
            PORT_DIPSETTING(0x04, "60/60");
            PORT_DIPSETTING(0x00, "55/60");
            PORT_DIPNAME(0x08, 0x08, "Demo Sounds?");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));

            PORT_START();  /* IN0 - X AXIS */

            PORT_ANALOG(0xff, 0x80, IPT_TRACKBALL_X | IPF_PLAYER1, 100, 63, 0, 0);

            PORT_START();  /* IN0 - Y AXIS */

            PORT_ANALOG(0xff, 0x80, IPT_TRACKBALL_Y | IPF_PLAYER1, 100, 63, 0, 0);

            PORT_START();  /* IN0 - BUTTON */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);

            PORT_START();  /* IN1 - X AXIS */

            PORT_ANALOG(0xff, 0x80, IPT_TRACKBALL_X | IPF_PLAYER2, 100, 63, 0, 0);

            PORT_START();  /* IN1 - Y AXIS */

            PORT_ANALOG(0xff, 0x80, IPT_TRACKBALL_Y | IPF_PLAYER2, 100, 63, 0, 0);

            PORT_START();  /* IN1 - BUTTON */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);

            PORT_START();  /* IN2 - Active LOW */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);

            PORT_START(); 	/* no fake port here */

            PORT_BIT(0xff, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_teedoff = new InputPortPtr() {
        public void handler() {
            PORT_START();  /* DSW1 - Active LOW */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();  /* DSW2 - Active LOW */

            PORT_DIPNAME(0xff, 0xff, DEF_STR("Unknown"));
            PORT_DIPSETTING(0xff, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();  /* DSW3 - Active LOW */

            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0f, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();  /* IN0 - X AXIS */

            PORT_ANALOG(0xff, 0x80, IPT_TRACKBALL_X | IPF_PLAYER1, 100, 63, 0, 0);

            PORT_START();  /* IN0 - Y AXIS */

            PORT_ANALOG(0xff, 0x80, IPT_TRACKBALL_Y | IPF_PLAYER1, 100, 63, 0, 0);

            PORT_START();  /* IN0 - BUTTON */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);

            PORT_START();  /* IN1 - X AXIS */

            PORT_ANALOG(0xff, 0x80, IPT_TRACKBALL_X | IPF_PLAYER2, 100, 63, 0, 0);

            PORT_START();  /* IN1 - Y AXIS */

            PORT_ANALOG(0xff, 0x80, IPT_TRACKBALL_Y | IPF_PLAYER2, 100, 63, 0, 0);

            PORT_START();  /* IN1 - BUTTON */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);

            PORT_START();  /* IN2 - Active LOW */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);

            PORT_START(); 	/* no fake port here */

            PORT_BIT(0xff, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            512, /* 512 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{1 * 4, 0 * 4, 3 * 4, 2 * 4, 5 * 4, 4 * 4, 7 * 4, 6 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every char takes 32 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            512, /* 512 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{1 * 4, 0 * 4, 3 * 4, 2 * 4, 5 * 4, 4 * 4, 7 * 4, 6 * 4,
                8 * 32 + 1 * 4, 8 * 32 + 0 * 4, 8 * 32 + 3 * 4, 8 * 32 + 2 * 4, 8 * 32 + 5 * 4, 8 * 32 + 4 * 4, 8 * 32 + 7 * 4, 8 * 32 + 6 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                16 * 32, 17 * 32, 18 * 32, 19 * 32, 20 * 32, 21 * 32, 22 * 32, 23 * 32},
            128 * 8 /* every char takes 32 consecutive bytes */
    );

    static GfxLayout tilelayout = new GfxLayout(
            16, 8, /* 16*8 characters */
            1024, /* 1024 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{1 * 4, 0 * 4, 3 * 4, 2 * 4, 5 * 4, 4 * 4, 7 * 4, 6 * 4,
                32 * 8 + 1 * 4, 32 * 8 + 0 * 4, 32 * 8 + 3 * 4, 32 * 8 + 2 * 4, 32 * 8 + 5 * 4, 32 * 8 + 4 * 4, 32 * 8 + 7 * 4, 32 * 8 + 6 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            64 * 8 /* every char takes 64 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 16), /* Colors 0 - 255 */
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 256, 8), /* Colors 256 - 383 */
                new GfxDecodeInfo(REGION_GFX3, 0, tilelayout, 512, 16), /* Colors 512 - 767 */
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            1536000, /* ??? */
            new int[]{25, 25},
            new ReadHandlerPtr[]{null, tehkanwc_portA_r},
            new ReadHandlerPtr[]{null, tehkanwc_portB_r},
            new WriteHandlerPtr[]{tehkanwc_portA_w, null},
            new WriteHandlerPtr[]{tehkanwc_portB_w, null}
    );

    static MSM5205interface msm5205_interface = new MSM5205interface(
            1, /* 1 chip             */
            384000, /* 384KHz             */
            new vclk_interruptPtr[]{tehkanwc_adpcm_int},/* interrupt function */
            new int[]{MSM5205_S48_4B}, /* 8KHz               */
            new int[]{25}
    );

    static MachineDriver machine_driver_tehkanwc = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4608000, /* 18.432000 / 4 */
                        readmem, writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        4608000, /* 18.432000 / 4 */
                        readmem_sub, writemem_sub, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80, /* communication is bidirectional, can't mark it as AUDIO_CPU */
                        4608000, /* 18.432000 / 4 */
                        readmem_sound, writemem_sound, sound_readport, sound_writeport,
                        interrupt, 1
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            10, /* 10 CPU slices per frame - seems enough to keep the CPUs in sync */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            768, 768,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            tehkanwc_vh_start,
            tehkanwc_vh_stop,
            tehkanwc_vh_screenrefresh,
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
    static RomLoadPtr rom_tehkanwc = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("twc-1.bin", 0x0000, 0x4000, 0x34d6d5ff);
            ROM_LOAD("twc-2.bin", 0x4000, 0x4000, 0x7017a221);
            ROM_LOAD("twc-3.bin", 0x8000, 0x4000, 0x8b662902);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for code */

            ROM_LOAD("twc-4.bin", 0x0000, 0x8000, 0x70a9f883);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for code */

            ROM_LOAD("twc-6.bin", 0x0000, 0x4000, 0xe3112be2);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("twc-12.bin", 0x00000, 0x4000, 0xa9e274f8);/* fg tiles */

            ROM_REGION(0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("twc-8.bin", 0x00000, 0x8000, 0x055a5264);/* sprites */

            ROM_LOAD("twc-7.bin", 0x08000, 0x8000, 0x59faebe7);

            ROM_REGION(0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("twc-11.bin", 0x00000, 0x8000, 0x669389fc);/* bg tiles */

            ROM_LOAD("twc-9.bin", 0x08000, 0x8000, 0x347ef108);

            ROM_REGION(0x8000, REGION_SOUND1);/* ADPCM samples */

            ROM_LOAD("twc-5.bin", 0x0000, 0x4000, 0x444b5544);
            ROM_END();
        }
    };

    static RomLoadPtr rom_gridiron = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("gfight1.bin", 0x0000, 0x4000, 0x51612741);
            ROM_LOAD("gfight2.bin", 0x4000, 0x4000, 0xa678db48);
            ROM_LOAD("gfight3.bin", 0x8000, 0x4000, 0x8c227c33);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for code */

            ROM_LOAD("gfight4.bin", 0x0000, 0x4000, 0x8821415f);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for code */

            ROM_LOAD("gfight5.bin", 0x0000, 0x4000, 0x92ca3c07);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gfight7.bin", 0x00000, 0x4000, 0x04390cca);/* fg tiles */

            ROM_REGION(0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gfight8.bin", 0x00000, 0x4000, 0x5de6a70f);/* sprites */

            ROM_LOAD("gfight9.bin", 0x04000, 0x4000, 0xeac9dc16);
            ROM_LOAD("gfight10.bin", 0x08000, 0x4000, 0x61d0690f);
            /* 0c000-0ffff empty */

            ROM_REGION(0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gfight11.bin", 0x00000, 0x4000, 0x80b09c03);/* bg tiles */

            ROM_LOAD("gfight12.bin", 0x04000, 0x4000, 0x1b615eae);
            /* 08000-0ffff empty */

            ROM_REGION(0x8000, REGION_SOUND1);/* ADPCM samples */

            ROM_LOAD("gfight6.bin", 0x0000, 0x4000, 0xd05d463d);
            ROM_END();
        }
    };

    static RomLoadPtr rom_teedoff = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("to-1.bin", 0x0000, 0x4000, 0xcc2aebc5);
            ROM_LOAD("to-2.bin", 0x4000, 0x4000, 0xf7c9f138);
            ROM_LOAD("to-3.bin", 0x8000, 0x4000, 0xa0f0a6da);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for code */

            ROM_LOAD("to-4.bin", 0x0000, 0x8000, 0xe922cbd2);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for code */

            ROM_LOAD("to-6.bin", 0x0000, 0x4000, 0xd8dfe1c8);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("to-12.bin", 0x00000, 0x4000, 0x4f44622c);/* fg tiles */

            ROM_REGION(0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("to-8.bin", 0x00000, 0x8000, 0x363bd1ba);/* sprites */

            ROM_LOAD("to-7.bin", 0x08000, 0x8000, 0x6583fa5b);

            ROM_REGION(0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("to-11.bin", 0x00000, 0x8000, 0x1ec00cb5);/* bg tiles */

            ROM_LOAD("to-9.bin", 0x08000, 0x8000, 0xa14347f0);

            ROM_REGION(0x8000, REGION_SOUND1);/* ADPCM samples */

            ROM_LOAD("to-5.bin", 0x0000, 0x8000, 0xe5e4246b);
            ROM_END();
        }
    };

    public static GameDriver driver_tehkanwc = new GameDriver("1985", "tehkanwc", "tehkanwc.java", rom_tehkanwc, null, machine_driver_tehkanwc, input_ports_tehkanwc, null, ROT0, "Tehkan", "Tehkan World Cup");
    public static GameDriver driver_gridiron = new GameDriver("1985", "gridiron", "tehkanwc.java", rom_gridiron, null, machine_driver_tehkanwc, input_ports_gridiron, null, ROT0, "Tehkan", "Gridiron Fight");
    public static GameDriver driver_teedoff = new GameDriver("1986", "teedoff", "tehkanwc.java", rom_teedoff, null, machine_driver_tehkanwc, input_ports_teedoff, null, ROT90, "Tecmo", "Tee'd Off", GAME_NOT_WORKING);
}
