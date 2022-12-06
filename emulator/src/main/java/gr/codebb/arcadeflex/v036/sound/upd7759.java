package gr.codebb.arcadeflex.v036.sound;

import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import gr.codebb.arcadeflex.v036.mame.sndintrf;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.upd7759H.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;

/**
 *
 * @author shadow
 */
public class upd7759 extends sndintrf.snd_interface {

    /* number of samples stuffed into the rom */
    static /*unsigned char*/ int numsam;

    /* playback rate for the streams interface */
 /* BASE_CLOCK or a multiple (if oversampling is active) */
    static int emulation_rate;

    static int base_rate;
    /* define the output rate */
    public static final int CLOCK_DIVIDER = 80;

    /* signal fall off factor */
    public static final int FALL_OFF(int n) {
        return ((n) - (((n) + 7) / 8));
    }

    public static final int SIGNAL_BITS = 15;
    /* signal range */

    public static final int SIGNAL_MAX = (0x7fff >> (15 - SIGNAL_BITS));
    public static final int SIGNAL_MIN = -SIGNAL_MAX;

    public static final int STEP_MAX = 32;
    public static final int STEP_MIN = 0;

    public static final int DATA_MAX = 512;

    public static class UPD7759sample {

        public UPD7759sample() {

        }

        public UPD7759sample(int offset, int length, int freq) {
            this.offset = offset;
            this.length = length;
            this.freq = freq;
        }
        /*unsigned*/ int offset;
        /* offset in that region */
 /*unsigned*/ int length;
        /* length of the sample */
 /*unsigned*/ int freq;
        /* play back freq of sample */

    };

    /* struct describing a single playing ADPCM voice */
    public static class UPD7759voice {

        int playing;
        /* 1 if we are actively playing */

        UBytePtr base;
        /* pointer to the base memory location */

        int mask;
        /* mask to keep us within the buffer */

        int sample;
        /* current sample number (sample data in slave mode) */

        int freq;
        /* current sample playback freq */

        int count;
        /* total samples to play */

        int signal;
        /* current ADPCM signal */

        int step;
        /* current ADPCM step */

        int counter;
        /* sample counter */

        Object timer;
        /* timer used in slave mode */

        int[] data = new int[DATA_MAX];
        /* data array used in slave mode */

        int /*unsigned*/ head;
        /* head of data array used in slave mode */

        int /*unsigned*/ tail;
        /* tail of data array used in slave mode */

        int /*unsigned*/ available;
    };
    /* global pointer to the current interface */
    static UPD7759_interface upd7759_intf;

    /* array of ADPCM voices */
    static UPD7759voice[] updadpcm = new UPD7759voice[MAX_UPD7759];

    /* array of channels returned by streams.c */
    static int[] channel = new int[MAX_UPD7759];

    /* stores the current sample number */
    static int[] sampnum = new int[MAX_UPD7759];

    /* step size index shift table */
    public static final int INDEX_SHIFT_MAX = 16;

    static int index_shift[] = {
        0, 1, 2, 3, 6, 7, 10, 15,
        0, 15, 10, 7, 6, 3, 2, 1
    };

    /* lookup table for the precomputed difference */
    static int[] diff_lookup = new int[(STEP_MAX + 1) * 16];

    /*
     *   Compute the difference table
     */
    static int nbl2bit[][] = {
        new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 1}, new int[]{1, 0, 1, 0}, new int[]{1, 0, 1, 1},
        new int[]{1, 1, 0, 0}, new int[]{1, 1, 0, 1}, new int[]{1, 1, 1, 0}, new int[]{1, 1, 1, 1},
        new int[]{-1, 0, 0, 0}, new int[]{-1, 0, 0, 1}, new int[]{-1, 0, 1, 0}, new int[]{-1, 0, 1, 1},
        new int[]{-1, 1, 0, 0}, new int[]{-1, 1, 0, 1}, new int[]{-1, 1, 1, 0}, new int[]{-1, 1, 1, 1},};

    static void ComputeTables() {
        /* nibble to bit map */

        int step, nib;

        /* loop over all possible steps */
        for (step = 0; step <= STEP_MAX; step++) {
            /* compute the step value */
            int stepval = 6 * (step + 1) * (step + 1);
            //LOG(1,(errorlog, "step %2d:", step));
            /* loop over all nibbles and compute the difference */
            for (nib = 0; nib < 16; nib++) {
                diff_lookup[step * 16 + nib] = nbl2bit[nib][0]
                        * (stepval * nbl2bit[nib][1]
                        + stepval / 2 * nbl2bit[nib][2]
                        + stepval / 4 * nbl2bit[nib][3]
                        + stepval / 8);
                //LOG(1,(errorlog, " %+6d", diff_lookup[step*16 + nib]));
            }
            //LOG(1,(errorlog, "\n"));
        }
    }

    static int find_sample(int num, int sample_num, UPD7759sample sample) {
        int j;
        int nextoff = 0;
        UBytePtr memrom;
        UBytePtr header;
        /* upd7759 has a 4 byte what we assume is an identifier (bytes 1-4)*/

        UBytePtr data;

        memrom = memory_region(upd7759_intf.region[num]);
        numsam = memrom.read(0);
        /* get number of samples from sound rom */
        //header = &(memrom[1]);
        /*if (memcmp (header, "\x5A\xA5\x69\x55",4) == 0)
         {
         LOG(1,(errorlog,"uPD7759 header verified\n"));
         }
         else
         {
         LOG(1,(errorlog,"uPD7759 header verification failed\n"));
         }

         LOG(1,(errorlog,"Number of samples in UPD7759 rom = %d\n",numsam));*/


 /* move the header pointer to the start of the sample offsets */
        header = new UBytePtr(memrom, 5);//header = &(memrom[5]);

        if (sample_num > numsam) {
            return 0;
            /* sample out of range */

        }

        nextoff = 2 * sample_num;
        sample.offset = ((((/*unsigned*/int) (header.read(nextoff))) << 8) + (header.read(nextoff + 1))) * 2;
        data = new UBytePtr(memory_region(upd7759_intf.region[num]), sample.offset);
        /* guesswork, probably wrong */
        j = 0;
        if (data.read(j) == 0) {
            j++;
        }
        if ((data.read(j) & 0xf0) != 0x50) {
            j++;
        }

        switch (data.read(j) & 0x1f) {
            case 0x13:
                sample.freq = 8000;
                break;
            case 0x19:
                sample.freq = 6000;
                break;
            case 0x1f:
                sample.freq = 5000;
                break;
            default:				// ???
                sample.freq = 5000;
        }

        if (sample_num == numsam) {
            sample.length = 0x20000 - sample.offset;
        } else {
            sample.length = ((((/*unsigned*/int) (header.read(nextoff + 2))) << 8) + (header.read(nextoff + 3))) * 2
                    - ((((/*unsigned*/int) (header.read(nextoff))) << 8) + (header.read(nextoff + 1))) * 2;
        }

        /*if (errorlog)
         {
         data = &memory_region(upd7759_intf->region[num])[sample->offset];
         fprintf( errorlog,"play sample %3d, offset $%06x, length %5d, freq = %4d [data $%02x $%02x $%02x]\n",
         sample_num,
         sample->offset,
         sample->length,
         sample->freq,
         data[0],data[1],data[2]);
         }*/
        return 1;
    }

    public upd7759() {
        sound_num = SOUND_UPD7759;
        name = "uPD7759";
        for (int i = 0; i < MAX_UPD7759; i++) {
            updadpcm[i] = new UPD7759voice();
        }
    }

    @Override
    public int chips_num(MachineSound msound) {
        return 0;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((UPD7759_interface) msound.sound_interface).clock_rate;
    }

    /*
     *   Start emulation of several ADPCM output streams
     */
    @Override
    public int start(MachineSound msound) {
        int i;
        UPD7759_interface intf = (UPD7759_interface) msound.sound_interface;

        if (Machine.sample_rate == 0) {
            return 0;
        }

        /* compute the difference tables */
        ComputeTables();

        /* copy the interface pointer to a global */
        upd7759_intf = intf;
        base_rate = intf.clock_rate / CLOCK_DIVIDER;

        emulation_rate = base_rate;

        //memset(updadpcm,0,sizeof(updadpcm));
        for (i = 0; i < intf.num; i++) {
            String name;
            updadpcm[i].mask = 0xffffffff;
            updadpcm[i].signal = 0;
            updadpcm[i].step = 0;
            updadpcm[i].counter = emulation_rate / 2;

            name = sprintf("uPD7759 #%d", i);

            channel[i] = stream_init(name, intf.volume[i], emulation_rate, i, UPD7759_update);
        }
        return 0;
    }
    /*
     *   Update emulation of an uPD7759 output stream
     */
    public static StreamInitPtr UPD7759_update = new StreamInitPtr() {
        public void handler(int chip, ShortPtr buffer, int left) {
            //struct UPD7759voice *voice = &updadpcm[chip];
            int i;

            /* see if there's actually any need to generate samples */
            //LOG(3,(errorlog,"UPD7759_update %d (%d)\n", left, voice->available));
            if (left > 0) {
                /* if this voice is active */
                if (updadpcm[chip].playing != 0) {
                    updadpcm[chip].available -= left;
                    if (upd7759_intf.mode == UPD7759_SLAVE_MODE) {
                        while (left-- > 0) {
                            buffer.write(0, (short) updadpcm[chip].data[updadpcm[chip].tail]);
                            buffer.offset += 2;
                            updadpcm[chip].tail = (updadpcm[chip].tail + 1) % DATA_MAX;
                        }
                    } else {
                        UBytePtr base = new UBytePtr(updadpcm[chip].base);
                        int val;

                        while (left > 0) {
                            /* compute the new amplitude and update the current updadpcm[chip].step */
                            val = base.read((updadpcm[chip].sample / 2) & updadpcm[chip].mask) >> (((updadpcm[chip].sample & 1) << 2) ^ 4);
                            updadpcm[chip].step = FALL_OFF(updadpcm[chip].step) + index_shift[val & (INDEX_SHIFT_MAX - 1)];
                            if (updadpcm[chip].step > STEP_MAX) {
                                updadpcm[chip].step = STEP_MAX;
                            } else if (updadpcm[chip].step < STEP_MIN) {
                                updadpcm[chip].step = STEP_MIN;
                            }
                            updadpcm[chip].signal = FALL_OFF(updadpcm[chip].signal) + diff_lookup[updadpcm[chip].step * 16 + (val & 15)];
                            if (updadpcm[chip].signal > SIGNAL_MAX) {
                                updadpcm[chip].signal = SIGNAL_MAX;
                            } else if (updadpcm[chip].signal < SIGNAL_MIN) {
                                updadpcm[chip].signal = SIGNAL_MIN;
                            }

                            while (updadpcm[chip].counter > 0 && left > 0) {
                                buffer.write(0, (short) updadpcm[chip].signal);
                                buffer.offset += 2;
                                updadpcm[chip].counter -= updadpcm[chip].freq;
                                left--;
                            }

                            updadpcm[chip].counter += emulation_rate;

                            /* next! */
                            if (++updadpcm[chip].sample > updadpcm[chip].count) {
                                while (left-- > 0) {
                                    buffer.write(0, (short) updadpcm[chip].signal);
                                    buffer.offset += 2;
                                    updadpcm[chip].signal = FALL_OFF(updadpcm[chip].signal);
                                }
                                updadpcm[chip].playing = 0;
                                break;
                            }
                        }
                    }
                } else {
                    /* voice is not playing */
                    for (i = 0; i < left; i++) {
                        buffer.write(0, (short) updadpcm[chip].signal);
                        buffer.offset += 2;
                    }
                }
            }
        }
    };

    /*
     *   Stop emulation of several UPD7759 output streams
     */
    @Override
    public void stop() {

    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }
    /**
     * **********************************************************
     * UPD7759_message_w
     *
     * Store the inputs to I0-I7 externally to the uPD7759.
     *
     * I0-I7 input the message number of the message to be reproduced. The
     * inputs are latched at the rising edge of the !ST input. Unused pins
     * should be grounded.
     *
     * In slave mode it seems like the ADPCM data is stuffed here from an
     * external source (eg. Z80 NMI code).
     * ***********************************************************
     */
    public static WriteHandlerPtr UPD7759_message_w = new WriteHandlerPtr() {
        public void handler(int num, int data) {

            //	struct UPD7759voice *voice = updadpcm + num;

            /* bail if we're not playing anything */
            if (Machine.sample_rate == 0) {
                return;
            }

            /* range check the numbers */
            if (num >= upd7759_intf.num) {
                //LOG(1,(errorlog,"error: UPD7759_SNDSELECT() called with channel = %d, but only %d channels allocated\n", num, upd7759_intf->num));
                return;
            }

            if (upd7759_intf.mode == UPD7759_SLAVE_MODE) {
                int offset = -1;

                //LOG(1,(errorlog,"upd7759_message_w $%02x\n", data));
                if (errorlog != null) {
                    fprintf(errorlog, "upd7759_message_w $%2x\n", data);
                }

                switch (data) {

                    case 0x00:
                    /* roms 0x10000 & 0x20000 in size */
                    case 0x38:
                        offset = 0x10000;
                        break;
                    /* roms 0x8000 in size */

                    case 0x01:
                    /* roms 0x10000 & 0x20000 in size */
                    case 0x39:
                        offset = 0x14000;
                        break;
                    /* roms 0x8000 in size */

                    case 0x02:
                    /* roms 0x10000 & 0x20000 in size */
                    case 0x34:
                        offset = 0x18000;
                        break;
                    /* roms 0x8000 in size */

                    case 0x03:
                    /* roms 0x10000 & 0x20000 in size */
                    case 0x35:
                        offset = 0x1c000;
                        break;
                    /* roms 0x8000 in size */

                    case 0x04:
                    /* roms 0x10000 & 0x20000 in size */
                    case 0x2c:
                        offset = 0x20000;
                        break;
                    /* roms 0x8000 in size */

                    case 0x05:
                    /* roms 0x10000 & 0x20000 in size */
                    case 0x2d:
                        offset = 0x24000;
                        break;
                    /* roms 0x8000 in size */

                    case 0x06:
                    /* roms 0x10000 & 0x20000 in size */
                    case 0x1c:
                        offset = 0x28000;
                        break;
                    /* roms 0x8000 in size in size */

                    case 0x07:
                    /* roms 0x10000 & 0x20000 in size */
                    case 0x1d:
                        offset = 0x2c000;
                        break;
                    /* roms 0x8000 in size */

                    case 0x08:
                        offset = 0x30000;
                        break;
                    /* roms 0x10000 & 0x20000 in size */
                    case 0x09:
                        offset = 0x34000;
                        break;
                    /* roms 0x10000 & 0x20000 in size */
                    case 0x0a:
                        offset = 0x38000;
                        break;
                    /* roms 0x10000 & 0x20000 in size */
                    case 0x0b:
                        offset = 0x3c000;
                        break;
                    /* roms 0x10000 & 0x20000 in size */
                    case 0x0c:
                        offset = 0x40000;
                        break;
                    /* roms 0x10000 & 0x20000 in size */
                    case 0x0d:
                        offset = 0x44000;
                        break;
                    /* roms 0x10000 & 0x20000 in size */
                    case 0x0e:
                        offset = 0x48000;
                        break;
                    /* roms 0x10000 & 0x20000 in size */
                    case 0x0f:
                        offset = 0x4c000;
                        break;
                    /* roms 0x10000 & 0x20000 in size */

                    default:

                        //LOG(1,(errorlog, "upd7759_message_w unhandled $%02x\n", data));
                        //if (errorlog) fprintf (errorlog, "upd7759_message_w unhandled $%02x\n", data);
                        if ((data & 0xc0) == 0xc0) {
                            if (updadpcm[num].timer != null) {
                                timer_remove(updadpcm[num].timer);
                                updadpcm[num].timer = null;
                            }
                            updadpcm[num].playing = 0;
                        }
                }
                if (offset > 0) {
                    updadpcm[num].base = new UBytePtr(memory_region(upd7759_intf.region[num]), offset);
                    //LOG(1,(errorlog, "upd7759_message_w set base $%08x\n", offset));
                    if (errorlog != null) {
                        fprintf(errorlog, "upd7759_message_w set base $%08x\n", offset);
                    }
                }
            } else {

                //LOG(1,(errorlog,"uPD7759 calling sample : %d\n", data));
                sampnum[num] = data;

            }
        }
    };

    /*TODO*////************************************************************
/*TODO*/// UPD7759_dac
/*TODO*///
/*TODO*/// Called by the timer interrupt at twice the sample rate.
/*TODO*/// The first time the external irq callback is called, the
/*TODO*/// second time the ADPCM msb is converted and the resulting
/*TODO*/// signal is sent to the DAC.
/*TODO*/// ************************************************************/
    static int dac_msb = 0;
    public static TimerCallbackHandlerPtr UPD7759_dac = new TimerCallbackHandlerPtr() {
        public void handler(int num) {

            //struct UPD7759voice *voice = updadpcm + num;
            dac_msb ^= 1;
            if (dac_msb != 0) {
                //LOG(3,(errorlog,"UPD7759_dac:    $%x ", voice->sample & 15));
                /* convert lower nibble */
                updadpcm[num].step = FALL_OFF(updadpcm[num].step) + index_shift[updadpcm[num].sample & (INDEX_SHIFT_MAX - 1)];
                if (updadpcm[num].step > STEP_MAX) {
                    updadpcm[num].step = STEP_MAX;
                } else if (updadpcm[num].step < STEP_MIN) {
                    updadpcm[num].step = STEP_MIN;
                }
                updadpcm[num].signal = FALL_OFF(updadpcm[num].signal) + diff_lookup[updadpcm[num].step * 16 + (updadpcm[num].sample & 15)];
                if (updadpcm[num].signal > SIGNAL_MAX) {
                    updadpcm[num].signal = SIGNAL_MAX;
                } else if (updadpcm[num].signal < SIGNAL_MIN) {
                    updadpcm[num].signal = SIGNAL_MIN;
                }
                //LOG(3,(errorlog,"step: %3d signal: %+5d\n", updadpcm[num].step, updadpcm[num].signal));
                updadpcm[num].head = (updadpcm[num].head + 1) % DATA_MAX;
                updadpcm[num].data[updadpcm[num].head] = updadpcm[num].signal;
                updadpcm[num].available++;
            } else if (upd7759_intf.irqcallback[num] != null) {
                (upd7759_intf.irqcallback[num]).handler(num);
            }
        }
    };
    /*TODO*///
/*TODO*////************************************************************
/*TODO*/// UPD7759_start_w
/*TODO*///
/*TODO*/// !ST pin:
/*TODO*/// Setting the !ST input low while !CS is low will start
/*TODO*/// speech reproduction of the message in the speech ROM locations
/*TODO*/// addressed by the contents of I0-I7. If the device is in
/*TODO*/// standby mode, standby mode will be released.
/*TODO*/// NOTE: While !BUSY is low, another !ST will not be accepted.
/*TODO*/// *************************************************************/
    public static WriteHandlerPtr UPD7759_start_w = new WriteHandlerPtr() {
        public void handler(int num, int data) {
            //struct UPD7759voice *voice = updadpcm + num;
            /* bail if we're not playing anything */
            if (Machine.sample_rate == 0) {
                return;
            }

            /* range check the numbers */
            if (num >= upd7759_intf.num) {
                //LOG(1,(errorlog,"error: UPD7759_play_stop() called with channel = %d, but only %d channels allocated\n", num, upd7759_intf->num));
                return;
            }

            /* handle the slave mode */
            if (upd7759_intf.mode == UPD7759_SLAVE_MODE) {
                if (updadpcm[num].playing != 0) {
                    /* if the chip is busy this should be the ADPCM data */
                    data &= 0xff;
                    /* be sure to use 8 bits value only */
                    //LOG(3,(errorlog,"UPD7759_data_w: $%x ", (data >> 4) & 15));

                    /* detect end of a sample by inspection of the last 5 bytes */
 /* FF 00 00 00 00 is the start of the next sample */
                    if (updadpcm[num].count > 5 && updadpcm[num].sample == 0xff && data == 0x00) {
                        /* remove an old timer */
                        if (updadpcm[num].timer != null) {
                            timer_remove(updadpcm[num].timer);
                            updadpcm[num].timer = null;
                        }
                        /* stop playing this sample */
                        updadpcm[num].playing = 0;
                        return;
                    }

                    /* save the data written in updadpcm[num].sample */
                    updadpcm[num].sample = data;
                    updadpcm[num].count++;

                    /* conversion of the ADPCM data to a new signal value */
                    updadpcm[num].step = FALL_OFF(updadpcm[num].step) + index_shift[(updadpcm[num].sample >> 4) & (INDEX_SHIFT_MAX - 1)];
                    if (updadpcm[num].step > STEP_MAX) {
                        updadpcm[num].step = STEP_MAX;
                    } else if (updadpcm[num].step < STEP_MIN) {
                        updadpcm[num].step = STEP_MIN;
                    }
                    updadpcm[num].signal = FALL_OFF(updadpcm[num].signal) + diff_lookup[updadpcm[num].step * 16 + ((updadpcm[num].sample >> 4) & 15)];
                    if (updadpcm[num].signal > SIGNAL_MAX) {
                        updadpcm[num].signal = SIGNAL_MAX;
                    } else if (updadpcm[num].signal < SIGNAL_MIN) {
                        updadpcm[num].signal = SIGNAL_MIN;
                    }
                    //LOG(3,(errorlog,"step: %3d signal: %+5d\n", updadpcm[num].step, updadpcm[num].signal));
                    updadpcm[num].head = (updadpcm[num].head + 1) % DATA_MAX;
                    updadpcm[num].data[updadpcm[num].head] = updadpcm[num].signal;
                    updadpcm[num].available++;
                } else {
                    //LOG(2,(errorlog,"UPD7759_start_w: $%02x\n", data));
                    /* remove an old timer */
                    if (updadpcm[num].timer != null) {
                        timer_remove(updadpcm[num].timer);
                        updadpcm[num].timer = null;
                    }
                    /* bring the chip in sync with the CPU */
                    stream_update(channel[num], 0);
                    /* start a new timer */
                    updadpcm[num].timer = timer_pulse(TIME_IN_HZ(base_rate), num, UPD7759_dac);
                    updadpcm[num].signal = 0;
                    updadpcm[num].step = 0;
                    /* reset the step width */
                    updadpcm[num].count = 0;
                    /* reset count for the detection of an sample ending */
                    updadpcm[num].playing = 1;
                    /* this voice is now playing */
                    updadpcm[num].tail = 0;
                    updadpcm[num].head = 0;
                    updadpcm[num].available = 0;
                }
            } else {
                UPD7759sample sample = new UPD7759sample();

                /* if !ST is high, do nothing */ /* EHC - 13/08/99 */
                if (data > 0) {
                    return;
                }

                /* bail if the chip is busy */
                if (updadpcm[num].playing != 0) {
                    return;
                }

                //LOG(2,(errorlog,"UPD7759_start_w: %d\n", data));

                /* find a match */
                if (find_sample(num, sampnum[num], sample) != 0) {
                    /* update the  voice */
                    stream_update(channel[num], 0);
                    updadpcm[num].freq = sample.freq;
                    /* set up the voice to play this sample */
                    updadpcm[num].playing = 1;
                    updadpcm[num].base = new UBytePtr(memory_region(upd7759_intf.region[num]), sample.offset);
                    updadpcm[num].sample = 0;
                    /* sample length needs to be doubled (counting nibbles) */
                    updadpcm[num].count = sample.length * 2;

                    /* also reset the chip parameters */
                    updadpcm[num].step = 0;
                    updadpcm[num].counter = emulation_rate / 2;

                    return;
                }

                //LOG(1,(errorlog,"warning: UPD7759_playing_w() called with invalid number = %08x\n",data));
            }
        }
    };

    /**
     * **********************************************************
     * UPD7759_data_r
     *
     * External read data from the UPD7759 memory region based on voice->base.
     * Used in slave mode to retrieve data to stuff into UPD7759_message_w.
     * ***********************************************************
     */
    public static int UPD7759_data_r(int num, int offs) {
        //struct UPD7759voice *voice = updadpcm + num;

        /* If there's no sample rate, do nothing */
        if (Machine.sample_rate == 0) {
            return 0x00;
        }

        /* range check the numbers */
        if (num >= upd7759_intf.num) {
            //LOG(1,(errorlog,"error: UPD7759_data_r() called with channel = %d, but only %d channels allocated\n", num, upd7759_intf->num));
            return 0x00;
        }

        if (updadpcm[num].base == null) {
            //LOG(1,(errorlog,"error: UPD7759_data_r() called with channel = %d, but updadpcm[%d].base == NULL\n", num, num));
            return 0x00;
        }

        /*#if VERBOSE
    if (!(offs&0xff)) LOG(1, (errorlog,"UPD7759#%d sample offset = $%04x\n", num, offs));
#endif*/
        return updadpcm[num].base.read(offs);
    }
    /* helper functions to be used as memory read handler function pointers */
    public static ReadHandlerPtr UPD7759_0_data_r = new ReadHandlerPtr() {
        public int handler(int offs) {
            return UPD7759_data_r(0, offs);
        }
    };
    public static ReadHandlerPtr UPD7759_1_data_r = new ReadHandlerPtr() {
        public int handler(int offs) {
            return UPD7759_data_r(1, offs);
        }
    };

    /**
     * **********************************************************
     * UPD7759_busy_r
     *
     * !BUSY pin: !BUSY outputs the status of the uPD7759. It goes low during
     * speech decode and output operations. When !ST is received, !BUSY goes
     * low. While !BUSY is low, another !ST will not be accepted. In standby
     * mode, !BUSY becomes high impedance. This is an active low output.
     * ***********************************************************
     */
    public static ReadHandlerPtr UPD7759_busy_r = new ReadHandlerPtr() {
        public int handler(int num) {
            //struct UPD7759voice *voice = updadpcm + num;
            /* If there's no sample rate, return not busy */
            if (Machine.sample_rate == 0) {
                return 1;
            }

            /* range check the numbers */
            if (num >= upd7759_intf.num) {
                //LOG(1,(errorlog,"error: UPD7759_busy_r() called with channel = %d, but only %d channels allocated\n", num, upd7759_intf->num));
                return 1;
            }

            /* bring the chip in sync with the CPU */
            stream_update(channel[num], 0);

            if (updadpcm[num].playing == 0) {
                //LOG(1,(errorlog,"uPD7759 not busy\n"));
                return 1;
            } else {
                //LOG(1,(errorlog,"uPD7759 busy\n"));
                return 0;
            }
        }
    };
    /**
     * **********************************************************
     * UPD7759_reset_w
     *
     * !RESET pin: The !RESET input initialized the chip. Use !RESET following
     * power-up to abort speech reproduction or to release standby mode. !RESET
     * must remain low at least 12 oscillator clocks. At power-up or when
     * recovering from standby mode, !RESET must remain low at least 12 more
     * clocks after clock oscillation stabilizes.
     * ***********************************************************
     */
    public static WriteHandlerPtr UPD7759_reset_w = new WriteHandlerPtr() {
        public void handler(int num, int data) {
            //struct UPD7759voice *voice = updadpcm + num;

            /* If there's no sample rate, do nothing */
            if (Machine.sample_rate == 0) {
                return;
            }

            /* range check the numbers */
            if (num >= upd7759_intf.num) {
                //LOG(1,(errorlog,"error: UPD7759_reset_w() called with channel = %d, but only %d channels allocated\n", num, upd7759_intf->num));
                return;
            }

            /* if !RESET is high, do nothing */
            if (data > 0) {
                return;
            }

            /* mark the uPD7759 as NOT PLAYING */
 /* (Note: do we need to do anything else?) */
            updadpcm[num].playing = 0;
        }
    };
}
