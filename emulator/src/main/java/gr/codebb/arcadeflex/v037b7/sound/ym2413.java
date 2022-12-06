/*
 *  Ported to 0.37b7
 */
package gr.codebb.arcadeflex.v037b7.sound;

import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.sound._2413intfH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v037b7.sound._3812intf.*;

public class ym2413 extends snd_interface {

    public static final int ym2413_channels = 9;
    /* 9 channels per chip */

    public static final int ym2413_parameter_count = 12;
    /* Number of values per row in instrument table */

    public static final int ym2413_clock = 3600000;

    /* 3.6 MHz */
    public class YM2413_state {

        public YM2413_state() {
            user_instrument = new int[ym2413_parameter_count];
            fnumberlow = new int[ym2413_channels];
        }
        public int fsam;
        public int rhythm_mode;
        public int pending_register;
        public int[] user_instrument;
        public int[] fnumberlow;
    }

    static YM2413_state[] ym2413_state = new YM2413_state[MAX_2413];

    public ym2413() {
        this.name = "YM-2413";
        this.sound_num = SOUND_YM2413;
        for (int i = 0; i < MAX_2413; i++) {
            ym2413_state[i] = new YM2413_state();
        }
    }
    static int ym2413_instruments[][]
            = {
                /* AM1  AM2  KS1  KS2  AD1  AD2  SR1  SR2  WS1  WS2   FB */
                new int[]{0x21, 0x01, 0x72, 0x04, 0xf1, 0x84, 0x7e, 0x6d, 0x00, 0x00, 0x00, 0x00}, /* 00 User          */
                new int[]{0x01, 0x22, 0x23, 0x07, 0xf0, 0xf0, 0x07, 0x18, 0x00, 0x00, 0x00, 0x00}, /* 01 Violin        */
                new int[]{0x23, 0x01, 0x68, 0x05, 0xf2, 0x74, 0x6c, 0x89, 0x00, 0x00, 0x00, 0x00}, /* 02 Guitar        */
                new int[]{0x13, 0x11, 0x25, 0x00, 0xd2, 0xb2, 0xf4, 0xf4, 0x00, 0x00, 0x00, 0x00}, /* 03 Piano         */
                new int[]{0x22, 0x21, 0x1b, 0x05, 0xc0, 0xa1, 0x18, 0x08, 0x00, 0x00, 0x00, 0x00}, /* 04 Flute         */
                new int[]{0x22, 0x21, 0x2c, 0x03, 0xd2, 0xa1, 0x18, 0x57, 0x00, 0x00, 0x00, 0x00}, /* 05 Clarinet      */
                new int[]{0x01, 0x22, 0xba, 0x01, 0xf1, 0xf1, 0x1e, 0x04, 0x00, 0x00, 0x00, 0x00}, /* 06 Oboe          */
                new int[]{0x21, 0x21, 0x28, 0x06, 0xf1, 0xf1, 0x6b, 0x3e, 0x00, 0x00, 0x00, 0x00}, /* 07 Trumpet       */
                new int[]{0x27, 0x21, 0x60, 0x00, 0xf0, 0xf0, 0x0d, 0x0f, 0x00, 0x00, 0x00, 0x00}, /* 08 Organ         */
                new int[]{0x20, 0x21, 0x2b, 0x06, 0x85, 0xf1, 0x6d, 0x89, 0x00, 0x00, 0x00, 0x00}, /* 09 Horn          */
                new int[]{0x01, 0x21, 0xbf, 0x02, 0x53, 0x62, 0x5f, 0xae, 0x01, 0x00, 0x00, 0x00}, /* 10 Synthesizer   */
                new int[]{0x23, 0x21, 0x70, 0x07, 0xd4, 0xa3, 0x4e, 0x64, 0x01, 0x00, 0x00, 0x00}, /* 11 Harpsicode    */
                new int[]{0x2b, 0x21, 0xa4, 0x07, 0xf6, 0x93, 0x5c, 0x4d, 0x00, 0x00, 0x00, 0x00}, /* 12 Vibraphone    */
                new int[]{0x21, 0x23, 0xad, 0x07, 0x77, 0xf1, 0x18, 0x37, 0x00, 0x00, 0x00, 0x00}, /* 13 SynthBass     */
                new int[]{0x21, 0x21, 0x2a, 0x03, 0xf3, 0xe2, 0x29, 0x46, 0x00, 0x00, 0x00, 0x00}, /* 14 AcousticBass  */
                new int[]{0x21, 0x23, 0x37, 0x03, 0xf3, 0xe2, 0x29, 0x46, 0x00, 0x00, 0x00, 0x00}, /* 15 ElectricGuitar*/
                /*
                 There are 5 rhythms using 3 channels. I'm not sure how this works.
                 I think that they may be shared, ie:
                 7=Bass drum
                 8=Hihat   / Snare drum
                 9=Tom-Tom / Top-Cymbal
                 */
                new int[]{0x13, 0x11, 0x25, 0x00, 0xd7, 0xb7, 0xf4, 0xf4, 0x00, 0x00, 0x00, 0x00}, /* 16 Rhythm 1: */
                new int[]{0x13, 0x11, 0x25, 0x00, 0xd7, 0xb7, 0xf4, 0xf4, 0x00, 0x00, 0x00, 0x00}, /* 17 Rhythm 2: */
                new int[]{0x13, 0x11, 0x25, 0x00, 0xd7, 0xb7, 0xf4, 0xf4, 0x00, 0x00, 0x00, 0x00}, /* 18 Rhythm 3: */};

    @Override
    public int chips_num(MachineSound msound) {
        return ((YM2413interface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((YM2413interface) msound.sound_interface).baseclock;
    }

    @Override
    public int start(MachineSound msound) {
        int i, j;
        for (i = 0; i < MAX_2413; i++) {
            /* Reset the chip state to 0 */
            ym2413_state[i] = new YM2413_state();//memset(&ym2413_state[i], 0, sizeof(ym2413_state[0]));

            /*
             Copy default values for the user instument. This is not strictly necessary,
             but will allow altering of the extra OPL settings that aren't
             set by the 2413 (feedback and wave select).
             */
            for (j = 0; j < ym2413_parameter_count; j++) {
                ym2413_state[i].user_instrument[j] = ym2413_instruments[0][j];
            }
            ym2413_state[i].fsam = ym2413_clock / 72;
        }
        return YM3812_sh_start(msound);
    }

    @Override
    public void stop() {
        int i;
        /* Clear down the OPL chip (emulated or real) */
        for (i = 0; i < 0xf5; i++) {
            OPL_WRITE(i, 0);
        }

        YM3812_sh_stop();
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }

    static void OPL_WRITE_DATA1(int offset, int channel, int data) {
        int[] order = {0x00, 0x01, 0x02, 0x08, 0x09, 0x0a, 0x10, 0x11, 0x12};
        OPL_WRITE(offset + order[channel], data);
    }

    static void OPL_WRITE_DATA2(int offset, int channel, int data) {
        int[] order = {0x03, 0x04, 0x05, 0x0b, 0x0c, 0x0d, 0x13, 0x14, 0x15};
        OPL_WRITE(offset + order[channel], data);
    }

    static void OPL_WRITE(int reg, int data) {
        YM3812_control_port_0_w.handler(0, reg);
        YM3812_write_port_0_w.handler(0, data);
    }

    static void ym2413_setinstrument(int chip, int channel, int inst) {
        int[] reg = {0x20, 0x20, 0x40, 0x40, 0x60, 0x60, 0x80, 0x80, 0xe0, 0xe0};
        int[] pn;
        int i;

        if (inst == 0) {
            /* Take values from user instrument settings for this chip */
            pn = ym2413_state[chip].user_instrument;
        } else {
            /* Lift stored instrument values from instrument table */
            pn = ym2413_instruments[inst];
        }
        int pni = 0;
        for (i = 0; i < 10; i++) {
            if ((i & 0x01) != 0) {
                OPL_WRITE_DATA2(reg[i], channel, pn[pni]);
            } else {
                OPL_WRITE_DATA1(reg[i], channel, pn[pni]);
            }
            pni++;
        }

        /* I dont know wich connection type the YM2413 has */
 /* So we leave feedback to the default for now */
 /*    OPL_WRITE(0xc0+channel, ( (*pn) << 1 )); */
    }
    public static ReadHandlerPtr YM2413_status_port_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM3812_status_port_0_r.handler(offset);
        }
    };
    public static WriteHandlerPtr YM2413_register_port_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int chip = offset;
            ym2413_state[chip].pending_register = data;
        }
    };
    public static WriteHandlerPtr YM2413_data_port_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int chip = offset;
            int value, channel, instrument, i, block, volume, frq, fnumber, octave;
            int pending = ym2413_state[chip].pending_register;

            switch (pending) {
                case 0x00:
                /* YM2413 ADSR registers */

                case 0x01:
                case 0x02:
                case 0x03:
                case 0x04:
                case 0x05:
                case 0x06:
                case 0x07:
                    ym2413_state[chip].user_instrument[pending] = data;
                    break;

                case 0x0e:
                    /* Rhythm control */

                    ym2413_state[chip].rhythm_mode = data & 0x20;
                    if ((data & 0x20) != 0) {
                        /* We are entering rhythm control */
                        for (i = 6; i < 9; i++) {
                            /* Turn off key select */
                            OPL_WRITE(0xb0 + i, 0);
                        }
                        ym2413_setinstrument(chip, 6, 16);
                        /* Rhythmn 1 */

                        ym2413_setinstrument(chip, 7, 17);
                        /* Rhythmn 2 */

                        ym2413_setinstrument(chip, 8, 18);
                        /* Rhythmn 3 */

                    }

                    OPL_WRITE(0xbd, data & 0x3f);
                    break;

                case 0x10:
                /* F-Number 8 bits */

                case 0x11:
                case 0x12:
                case 0x13:
                case 0x14:
                case 0x15:
                case 0x16:
                case 0x17:
                case 0x18:
                    channel = pending - 0x10;
                    ym2413_state[chip].fnumberlow[channel] = data;
                    break;

                case 0x20:
                /* FNumber / note on / off */

                case 0x21:
                case 0x22:
                case 0x23:
                case 0x24:
                case 0x25:
                case 0x26:
                case 0x27:
                case 0x28:
                    channel = pending - 0x20;
                    fnumber = ym2413_state[chip].fnumberlow[channel] | (data & 0x01) << 8;
                    block = (data >> 1) & 0x07;

                    /* Calculate frequency */
                    frq = fnumber * (2 << (block - 1))
                            * ym2413_state[chip].fsam / (2 << 18);

                    /* Convert into frequency & octave (+raise octaves) */
                    octave = ((frq >> 10) & 0x0f) + 3;
                    frq &= 0x3ff;

                    /* Build register */
                    value = (data & 0x10) << 1;
                    /* Translate key on-off */
 /* Sustain does not exist */

                    value |= (frq >> 8) & 0x03;
                    /* Add frequency high to control */

                    value |= octave << 2;
                    /* Add in the octave */

 /* Write the OPL registers */
                    OPL_WRITE(0xa0 + channel, frq & 0xff);
                    /* Frequency LSB */

                    OPL_WRITE(0xb0 + channel, value);
                    /* Frequency MSB + Key on/off */

                    break;

                case 0x30:
                /* INSTRUMENT / VOLUME */

                case 0x31:
                case 0x32:
                case 0x33:
                case 0x34:
                case 0x35:
                case 0x36:
                case 0x37:
                case 0x38:
                    channel = pending - 0x30;

                    if (ym2413_state[chip].rhythm_mode != 0 && channel >= 6) {
                        /*
                         Running in rhythm mode. 6, 7 and 8 contain the volumes
                         of the rhythm voices encoded in the hi/low nibbles
                         of the control byte.
                         0x06 = <NONE>              | BASS DRUM LEVEL   0-15
                         0x07 = HIHAT LEVEL   0-15  | SNARE DRUM LEVEL  0-15
                         0x08 = TOM-TOM LEVEL 0-15  | TOP-CYMBAL LEVEL  0-15
                         */

 /*
                         TODO: I don't know how to alter the volume of the individual
                         rhythm voices since they share channels on the OPL.
                         */
                    } else {
                        /* Write instrument data */
                        instrument = (data >> 4) & 0x0f;
                        ym2413_setinstrument(chip, channel, instrument);

                        /* Set the voice volume (0-15) */
                        volume = data & 0x0f;

                        /*
                         TODO: I don't know how to alter the volume for each voice
                         */
                    }
                    break;

                default:
                    logerror("YM2413: Write to register %02x\n", pending);
                    break;
            }
        }
    };
}
