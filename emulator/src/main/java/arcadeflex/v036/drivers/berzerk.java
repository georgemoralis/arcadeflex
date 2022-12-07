/*
 * ported to v0.36
 */
package arcadeflex.v036.drivers;

//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.commonH.REGION_CPU1;
import static gr.codebb.arcadeflex.v036.mame.commonH.ROM_END;
import static gr.codebb.arcadeflex.v036.mame.commonH.ROM_LOAD;
import static gr.codebb.arcadeflex.v036.mame.commonH.ROM_REGION;
import static gr.codebb.arcadeflex.v036.mame.driverH.CPU_Z80;
import static gr.codebb.arcadeflex.v036.mame.driverH.DEFAULT_60HZ_VBLANK_DURATION;
import gr.codebb.arcadeflex.v036.mame.driverH.GameDriver;
import gr.codebb.arcadeflex.v036.mame.driverH.InputPortPtr;
import gr.codebb.arcadeflex.v036.mame.driverH.MachineCPU;
import gr.codebb.arcadeflex.v036.mame.driverH.MachineDriver;
import static gr.codebb.arcadeflex.v036.mame.driverH.ROT0;
import gr.codebb.arcadeflex.v036.mame.driverH.RomLoadPtr;
import static gr.codebb.arcadeflex.v036.mame.driverH.VIDEO_SUPPORTS_DIRTY;
import static gr.codebb.arcadeflex.v036.mame.driverH.VIDEO_TYPE_RASTER;
import gr.codebb.arcadeflex.v036.mame.driverH.VhConvertColorPromPtr;
import gr.codebb.arcadeflex.v036.mame.driverH.nvramPtr;
import static gr.codebb.arcadeflex.v036.mame.inputH.KEYCODE_F1;
import static gr.codebb.arcadeflex.v036.mame.inputH.KEYCODE_F2;
import static gr.codebb.arcadeflex.v036.mame.inputH.KEYCODE_F4;
import static gr.codebb.arcadeflex.v036.mame.inputH.KEYCODE_F5;
import arcadeflex.v036.mame.sndintrfH.CustomSound_interface;
import arcadeflex.v036.mame.sndintrfH.MachineSound;
import static arcadeflex.v036.mame.sndintrfH.SOUND_CUSTOM;
import static arcadeflex.v036.mame.sndintrfH.SOUND_SAMPLES;
import static gr.codebb.arcadeflex.v036.platform.fileio.osd_fread;
import static gr.codebb.arcadeflex.v036.platform.fileio.osd_fwrite;
import gr.codebb.arcadeflex.v036.sound.samplesH.Samplesinterface;
import static arcadeflex.v036.machine.berzerk.*;
import gr.codebb.arcadeflex.v037b7.mame.drawgfxH.rectangle;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.input_port_0_r;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.input_port_1_r;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.input_port_2_r;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.input_port_3_r;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.input_port_4_r;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.input_port_5_r;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.input_port_6_r;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.input_port_7_r;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.input_port_8_r;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.DEF_STR;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.INPUT_PORTS_END;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IPF_8WAY;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IPF_COCKTAIL;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IPF_TOGGLE;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IPT_BUTTON1;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IPT_COIN1;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IPT_COIN2;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IPT_COIN3;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IPT_DIPSWITCH_NAME;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IPT_JOYSTICK_DOWN;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IPT_JOYSTICK_LEFT;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IPT_JOYSTICK_RIGHT;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IPT_JOYSTICK_UP;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IPT_START1;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IPT_START2;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IPT_UNUSED;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IP_ACTIVE_HIGH;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IP_ACTIVE_LOW;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.IP_JOY_NONE;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.PORT_BIT;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.PORT_BITX;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.PORT_DIPNAME;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.PORT_DIPSETTING;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.PORT_START;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.sndhrdw.berzerk.berzerk_sh_start;
import static arcadeflex.v036.sndhrdw.berzerk.berzerk_sh_update;
import static arcadeflex.v036.sndhrdw.berzerk.berzerk_sound_control_a_w;
import static arcadeflex.v036.vidhrdw.berzerk.berzerk_collision_r;
import static arcadeflex.v036.vidhrdw.berzerk.berzerk_colorram_w;
import static arcadeflex.v036.vidhrdw.berzerk.berzerk_magicram;
import static arcadeflex.v036.vidhrdw.berzerk.berzerk_magicram_control_w;
import static arcadeflex.v036.vidhrdw.berzerk.berzerk_magicram_w;
import static arcadeflex.v036.vidhrdw.berzerk.berzerk_vh_screenrefresh;
import static arcadeflex.v036.vidhrdw.berzerk.berzerk_videoram_w;

public class berzerk {

    static UBytePtr nvram = new UBytePtr();
    static int[] nvram_size = new int[1];

    public static nvramPtr berzerk_nvram_handler
            = new nvramPtr() {
        public void handler(Object file, int read_or_write) {
            if (read_or_write != 0) {
                osd_fwrite(file, nvram, nvram_size[0]);
            } else {
                if (file != null) {
                    osd_fread(file, nvram, nvram_size[0]);
                }
            }
        }
    };

    static MemoryReadAddress berzerk_readmem[] = {
        new MemoryReadAddress(0x0000, 0x07ff, MRA_ROM),
        new MemoryReadAddress(0x0800, 0x09ff, MRA_RAM),
        new MemoryReadAddress(0x1000, 0x3fff, MRA_ROM),
        new MemoryReadAddress(0x4000, 0x87ff, MRA_RAM),
        new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress berzerk_writemem[] = {
        new MemoryWriteAddress(0x0000, 0x07ff, MWA_ROM),
        new MemoryWriteAddress(0x0800, 0x09ff, MWA_RAM, nvram, nvram_size),
        new MemoryWriteAddress(0x1000, 0x3fff, MWA_ROM),
        new MemoryWriteAddress(0x4000, 0x5fff, berzerk_videoram_w, videoram, videoram_size),
        new MemoryWriteAddress(0x6000, 0x7fff, berzerk_magicram_w, berzerk_magicram),
        new MemoryWriteAddress(0x8000, 0x87ff, berzerk_colorram_w, colorram),
        new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress frenzy_readmem[] = {
        new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
        new MemoryReadAddress(0x4000, 0x87ff, MRA_RAM),
        new MemoryReadAddress(0xc000, 0xcfff, MRA_ROM),
        new MemoryReadAddress(0xf800, 0xf9ff, MRA_RAM),
        new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress frenzy_writemem[] = {
        new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
        new MemoryWriteAddress(0x4000, 0x5fff, berzerk_videoram_w, videoram, videoram_size),
        new MemoryWriteAddress(0x6000, 0x7fff, berzerk_magicram_w, berzerk_magicram),
        new MemoryWriteAddress(0x8000, 0x87ff, berzerk_colorram_w, colorram),
        new MemoryWriteAddress(0xc000, 0xcfff, MWA_ROM),
        new MemoryWriteAddress(0xf800, 0xf9ff, MWA_RAM),
        new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort readport[] = {
        new IOReadPort(0x44, 0x44, berzerk_voiceboard_read), /* Sound stuff */
        new IOReadPort(0x48, 0x48, input_port_0_r),
        new IOReadPort(0x49, 0x49, input_port_1_r),
        new IOReadPort(0x4a, 0x4a, input_port_2_r),
        new IOReadPort(0x4c, 0x4c, berzerk_nmi_enable_r),
        new IOReadPort(0x4d, 0x4d, berzerk_nmi_disable_r),
        new IOReadPort(0x4e, 0x4e, berzerk_collision_r),
        new IOReadPort(0x60, 0x60, input_port_3_r),
        new IOReadPort(0x61, 0x61, input_port_4_r),
        new IOReadPort(0x62, 0x62, input_port_5_r),
        new IOReadPort(0x63, 0x63, input_port_6_r),
        new IOReadPort(0x64, 0x64, input_port_7_r),
        new IOReadPort(0x65, 0x65, input_port_8_r),
        new IOReadPort(0x66, 0x66, berzerk_led_off_r),
        new IOReadPort(0x67, 0x67, berzerk_led_on_r),
        new IOReadPort(-1) /* end of table */};

    static IOWritePort writeport[] = {
        new IOWritePort(0x40, 0x46, berzerk_sound_control_a_w), /* First sound board */
        new IOWritePort(0x47, 0x47, IOWP_NOP), /* not used sound stuff */
        new IOWritePort(0x4b, 0x4b, berzerk_magicram_control_w),
        new IOWritePort(0x4c, 0x4c, berzerk_nmi_enable_w),
        new IOWritePort(0x4d, 0x4d, berzerk_nmi_disable_w),
        new IOWritePort(0x4f, 0x4f, berzerk_irq_enable_w),
        new IOWritePort(0x50, 0x57, IOWP_NOP), /* Second sound board but not used */
        new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_berzerk
            = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x1c, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x60, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));

            PORT_START();
            /* IN3 */
            PORT_BITX(
                    0x01,
                    0x00,
                    IPT_DIPSWITCH_NAME | IPF_TOGGLE,
                    "Input Test Mode",
                    KEYCODE_F2,
                    IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_BITX(
                    0x02,
                    0x00,
                    IPT_DIPSWITCH_NAME | IPF_TOGGLE,
                    "Crosshair Pattern",
                    KEYCODE_F4,
                    IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_BIT(0x3c, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_DIPNAME(0xc0, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x40, "German");
            PORT_DIPSETTING(0x80, "French");
            PORT_DIPSETTING(0xc0, "Spanish");

            PORT_START();
            /* IN4 */
            PORT_BITX(
                    0x03, 0x00, IPT_DIPSWITCH_NAME | IPF_TOGGLE, "Color Test", KEYCODE_F5, IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x03, DEF_STR("On"));
            PORT_BIT(0x3c, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0xc0, "5000 and 10000");
            PORT_DIPSETTING(0x40, "5000");
            PORT_DIPSETTING(0x80, "10000");
            PORT_DIPSETTING(0x00, "None");

            PORT_START();
            /* IN5 */
            PORT_DIPNAME(0x0f, 0x00, "Coin 3");
            PORT_DIPSETTING(0x09, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0d, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0e, DEF_STR("4C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("4C_7C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0b, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("2C_7C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x07, "1 Coin/10 Credits");
            PORT_DIPSETTING(0x08, "1 Coin/14 Credits");
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN6 */
            PORT_DIPNAME(0x0f, 0x00, "Coin 2");
            PORT_DIPSETTING(0x09, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0d, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0e, DEF_STR("4C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("4C_7C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0b, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("2C_7C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x07, "1 Coin/10 Credits");
            PORT_DIPSETTING(0x08, "1 Coin/14 Credits");
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN7 */
            PORT_DIPNAME(0x0f, 0x00, "Coin 1");
            PORT_DIPSETTING(0x09, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0d, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0e, DEF_STR("4C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("4C_7C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0b, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("2C_7C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x07, "1 Coin/10 Credits");
            PORT_DIPSETTING(0x08, "1 Coin/14 Credits");
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN8 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_BIT(0x7e, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BITX(0x80, IP_ACTIVE_HIGH, 0, "Stats", KEYCODE_F1, IP_JOY_NONE);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_frenzy
            = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x3c, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x60, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));

            PORT_START();
            /* IN3 */
            PORT_DIPNAME(0x0f, 0x03, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x01, "1000");
            PORT_DIPSETTING(0x02, "2000");
            PORT_DIPSETTING(0x03, "3000");
            PORT_DIPSETTING(0x04, "4000");
            PORT_DIPSETTING(0x05, "5000");
            PORT_DIPSETTING(0x06, "6000");
            PORT_DIPSETTING(0x07, "7000");
            PORT_DIPSETTING(0x08, "8000");
            PORT_DIPSETTING(0x09, "9000");
            PORT_DIPSETTING(0x0a, "10000");
            PORT_DIPSETTING(0x0b, "11000");
            PORT_DIPSETTING(0x0c, "12000");
            PORT_DIPSETTING(0x0d, "13000");
            PORT_DIPSETTING(0x0e, "14000");
            PORT_DIPSETTING(0x0f, "15000");
            PORT_DIPSETTING(0x00, "None");
            PORT_BIT(0x30, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_DIPNAME(0xc0, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x40, "German");
            PORT_DIPSETTING(0x80, "French");
            PORT_DIPSETTING(0xc0, "Spanish");

            PORT_START();
            /* IN4 */
            PORT_BIT(0x03, IP_ACTIVE_HIGH, IPT_UNUSED);
            /* Bit 0 does some more hardware tests */
            PORT_BITX(
                    0x04,
                    0x00,
                    IPT_DIPSWITCH_NAME | IPF_TOGGLE,
                    "Input Test Mode",
                    KEYCODE_F2,
                    IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_BITX(
                    0x08,
                    0x00,
                    IPT_DIPSWITCH_NAME | IPF_TOGGLE,
                    "Crosshair Pattern",
                    KEYCODE_F4,
                    IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            /* The following 3 ports use all 8 bits, but I didn't feel like adding all 256 values :-) */
            PORT_START();
            /* IN5 */
            PORT_DIPNAME(0x0f, 0x01, "Coins/Credit B");
            /*PORT_DIPSETTING(    0x00, "0" );   Can't insert coins  */
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_DIPSETTING(0x05, "5");
            PORT_DIPSETTING(0x06, "6");
            PORT_DIPSETTING(0x07, "7");
            PORT_DIPSETTING(0x08, "8");
            PORT_DIPSETTING(0x09, "9");
            PORT_DIPSETTING(0x0a, "10");
            PORT_DIPSETTING(0x0b, "11");
            PORT_DIPSETTING(0x0c, "12");
            PORT_DIPSETTING(0x0d, "13");
            PORT_DIPSETTING(0x0e, "14");
            PORT_DIPSETTING(0x0f, "15");
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN6 */
            PORT_DIPNAME(0x0f, 0x01, "Coins/Credit A");
            /*PORT_DIPSETTING(    0x00, "0" );   Can't insert coins  */
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_DIPSETTING(0x05, "5");
            PORT_DIPSETTING(0x06, "6");
            PORT_DIPSETTING(0x07, "7");
            PORT_DIPSETTING(0x08, "8");
            PORT_DIPSETTING(0x09, "9");
            PORT_DIPSETTING(0x0a, "10");
            PORT_DIPSETTING(0x0b, "11");
            PORT_DIPSETTING(0x0c, "12");
            PORT_DIPSETTING(0x0d, "13");
            PORT_DIPSETTING(0x0e, "14");
            PORT_DIPSETTING(0x0f, "15");
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN7 */
            PORT_DIPNAME(0x0f, 0x01, "Coin Multiplier");
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_DIPSETTING(0x05, "5");
            PORT_DIPSETTING(0x06, "6");
            PORT_DIPSETTING(0x07, "7");
            PORT_DIPSETTING(0x08, "8");
            PORT_DIPSETTING(0x09, "9");
            PORT_DIPSETTING(0x0a, "10");
            PORT_DIPSETTING(0x0b, "11");
            PORT_DIPSETTING(0x0c, "12");
            PORT_DIPSETTING(0x0d, "13");
            PORT_DIPSETTING(0x0e, "14");
            PORT_DIPSETTING(0x0f, "15");
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN8 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x7e, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BITX(0x80, IP_ACTIVE_HIGH, 0, "Stats", KEYCODE_F1, IP_JOY_NONE);
            INPUT_PORTS_END();
        }
    };

    /* Simple 1-bit RGBI palette */
    static char palette[] = {
        0x00, 0x00, 0x00,
        0xff, 0x00, 0x00,
        0x00, 0xff, 0x00,
        0xff, 0xff, 0x00,
        0x00, 0x00, 0xff,
        0xff, 0x00, 0xff,
        0x00, 0xff, 0xff,
        0xff, 0xff, 0xff,
        0x40, 0x40, 0x40,
        0xff, 0x40, 0x40,
        0x40, 0xff, 0x40,
        0xff, 0xff, 0x40,
        0x40, 0x40, 0xff,
        0xff, 0x40, 0xff,
        0x40, 0xff, 0xff,
        0xff, 0xff, 0xff
    };
    public static VhConvertColorPromPtr init_palette
            = new VhConvertColorPromPtr() {
        public void handler(char[] game_palette, char[] game_colortable, UBytePtr color_prom) {
            // memcpy(game_palette,palette,sizeof(palette));
            for (int i = 0; i < palette.length; i++) {
                game_palette[i] = palette[i];
            }
        }
    };

    static String berzerk_sample_names[] = {
        "*berzerk", /* universal samples directory */
        "",
        "01.wav", // "kill"
        "02.wav", // "attack"
        "03.wav", // "charge"
        "04.wav", // "got"
        "05.wav", // "to"
        "06.wav", // "get"
        "",
        "08.wav", // "alert"
        "09.wav", // "detected"
        "10.wav", // "the"
        "11.wav", // "in"
        "12.wav", // "it"
        "",
        "",
        "15.wav", // "humanoid"
        "16.wav", // "coins"
        "17.wav", // "pocket"
        "18.wav", // "intruder"
        "",
        "20.wav", // "escape"
        "21.wav", // "destroy"
        "22.wav", // "must"
        "23.wav", // "not"
        "24.wav", // "chicken"
        "25.wav", // "fight"
        "26.wav", // "like"
        "27.wav", // "a"
        "28.wav", // "robot"
        "",
        "30.wav", // player fire
        "31.wav", // baddie fire
        "32.wav", // kill baddie
        "33.wav", // kill human (real)
        "34.wav", // kill human (cheat)
        null /* end of array */};

    static Samplesinterface berzerk_samples_interface
            = new Samplesinterface(8, /* 8 channels */ 25, /* volume */ berzerk_sample_names);

    static CustomSound_interface custom_interface
            = new CustomSound_interface(berzerk_sh_start, null, berzerk_sh_update);

    static MachineDriver machine_driver_berzerk
            = new MachineDriver(
                    /* basic machine hardware */
                    new MachineCPU[]{
                        new MachineCPU(
                                CPU_Z80,
                                2500000, /* 2.5 MHz */
                                berzerk_readmem,
                                berzerk_writemem,
                                readport,
                                writeport,
                                berzerk_interrupt,
                                8),},
                    60,
                    DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
                    1, /* single CPU, no need for interleaving */
                    berzerk_init_machine,
                    /* video hardware */
                    256,
                    256,
                    new rectangle(0, 256 - 1, 32, 256 - 1),
                    null,
                    palette.length / 3,
                    0,
                    init_palette,
                    VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
                    null,
                    null,
                    null,
                    berzerk_vh_screenrefresh,
                    /* sound hardware */
                    0,
                    0,
                    0,
                    0,
                    new MachineSound[]{
                        new MachineSound(SOUND_SAMPLES, berzerk_samples_interface),
                        new MachineSound(SOUND_CUSTOM, /* actually plays the samples */ custom_interface)
                    },
                    berzerk_nvram_handler);
    static MachineDriver machine_driver_frenzy
            = new MachineDriver(
                    /* basic machine hardware */
                    new MachineCPU[]{
                        new MachineCPU(
                                CPU_Z80,
                                2500000, /* 2.5 MHz */
                                frenzy_readmem,
                                frenzy_writemem,
                                readport,
                                writeport,
                                berzerk_interrupt,
                                8),},
                    60,
                    DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
                    1, /* single CPU, no need for interleaving */
                    null,
                    /* video hardware */
                    256,
                    256,
                    new rectangle(0, 256 - 1, 32, 256 - 1),
                    null,
                    palette.length / 3,
                    0,
                    init_palette,
                    VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
                    null,
                    null,
                    null,
                    berzerk_vh_screenrefresh,
                    /* sound hardware */
                    0,
                    0,
                    0,
                    0,
                    new MachineSound[]{
                        new MachineSound(SOUND_SAMPLES, berzerk_samples_interface),
                        new MachineSound(SOUND_CUSTOM, /* actually plays the samples */ custom_interface)
                    },
                    null);

    /**
     * *************************************************************************
     *
     * <p>
     * Game driver(s)
     *
     * <p>
     *************************************************************************
     */
    static RomLoadPtr rom_berzerk
            = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            ROM_LOAD("1c-0", 0x0000, 0x0800, 0xca566dbc);
            ROM_LOAD("1d-1", 0x1000, 0x0800, 0x7ba69fde);
            ROM_LOAD("3d-2", 0x1800, 0x0800, 0xa1d5248b);
            ROM_LOAD("5d-3", 0x2000, 0x0800, 0xfcaefa95);
            ROM_LOAD("6d-4", 0x2800, 0x0800, 0x1e35b9a0);
            ROM_LOAD("5c-5", 0x3000, 0x0800, 0xc8c665e5);
            ROM_END();
        }
    };

    static RomLoadPtr rom_berzerk1
            = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            ROM_LOAD("rom0.1c", 0x0000, 0x0800, 0x5b7eb77d);
            ROM_LOAD("rom1.1d", 0x1000, 0x0800, 0xe58c8678);
            ROM_LOAD("rom2.3d", 0x1800, 0x0800, 0x705bb339);
            ROM_LOAD("rom3.5d", 0x2000, 0x0800, 0x6a1936b4);
            ROM_LOAD("rom4.6d", 0x2800, 0x0800, 0xfa5dce40);
            ROM_LOAD("rom5.5c", 0x3000, 0x0800, 0x2579b9f4);
            ROM_END();
        }
    };

    static RomLoadPtr rom_frenzy
            = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            ROM_LOAD("1c-0", 0x0000, 0x1000, 0xabdd25b8);
            ROM_LOAD("1d-1", 0x1000, 0x1000, 0x536e4ae8);
            ROM_LOAD("3d-2", 0x2000, 0x1000, 0x3eb9bc9b);
            ROM_LOAD("5d-3", 0x3000, 0x1000, 0xe1d3133c);
            ROM_LOAD("6d-4", 0xc000, 0x1000, 0x5581a7b1);
            /* 1c & 2c are the voice ROMs */
            ROM_END();
        }
    };

    public static GameDriver driver_berzerk
            = new GameDriver(
                    "1980",
                    "berzerk",
                    "berzerk.java",
                    rom_berzerk,
                    null,
                    machine_driver_berzerk,
                    input_ports_berzerk,
                    null,
                    ROT0,
                    "Stern",
                    "Berzerk (set 1)");
    public static GameDriver driver_berzerk1
            = new GameDriver(
                    "1980",
                    "berzerk1",
                    "berzerk.java",
                    rom_berzerk1,
                    driver_berzerk,
                    machine_driver_berzerk,
                    input_ports_berzerk,
                    null,
                    ROT0,
                    "Stern",
                    "Berzerk (set 2)");
    public static GameDriver driver_frenzy
            = new GameDriver(
                    "1982",
                    "frenzy",
                    "berzerk.java",
                    rom_frenzy,
                    null,
                    machine_driver_frenzy,
                    input_ports_frenzy,
                    null,
                    ROT0,
                    "Stern",
                    "Frenzy");
}
