/*
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.v037b7.sound;

import gr.codebb.arcadeflex.common.PtrLib.ShortPtr;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.sound._5220intfH.*;
import static gr.codebb.arcadeflex.v037b7.sound.tms5220.*;
import static arcadeflex.v036.sound.streams.*;

public class _5220intf extends snd_interface {

    public static final int MAX_SAMPLE_CHUNK = 10000;
    public static final int FRAC_BITS = 14;
    public static final int FRAC_ONE = (1 << FRAC_BITS);
    public static final int FRAC_MASK = (FRAC_ONE - 1);


    /* the state of the streamed output */
    static TMS5220interface intf;
    static short last_sample, curr_sample;
    static /*UINT32*/ int source_step;
    static /*UINT32*/ int source_pos;
    static int stream;

    public _5220intf() {
        this.name = "TMS5220";
        this.sound_num = SOUND_TMS5220;
    }

    @Override
    public int chips_num(MachineSound msound) {
        return 0;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((TMS5220interface) msound.sound_interface).baseclock;
    }

    @Override
    public int start(MachineSound msound) {
        intf = (TMS5220interface) msound.sound_interface;

        /* reset the 5220 */
        tms5220_reset();
        tms5220_set_irq(intf.irq);

        /* set the initial frequency */
        stream = -1;
        tms5220_set_frequency(intf.baseclock);
        source_pos = 0;
        last_sample = curr_sample = 0;

        /* initialize a stream */
        stream = stream_init("TMS5220", intf.mixing_level, Machine.sample_rate, 0, tms5220_update);
        if (stream == -1) {
            return 1;
        }

        /* request a sound channel */
        return 0;
    }

    @Override
    public void stop() {
        //no functionality expected
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
     * ********************************************************************************************
     * tms5220_data_w -- write data to the sound chip
     * *********************************************************************************************
     */
    public static WriteHandlerPtr tms5220_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bring up to date first */
            stream_update(stream, 0);
            tms5220_data_write(data);
        }
    };

    /**
     * ********************************************************************************************
     * tms5220_status_r -- read status from the sound chip
     * *********************************************************************************************
     */
    public static ReadHandlerPtr tms5220_status_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* bring up to date first */
            stream_update(stream, -1);
            return tms5220_status_read();
        }
    };

    /**
     * ********************************************************************************************
     * tms5220_ready_r -- return the not ready status from the sound chip
     * *********************************************************************************************
     */
    public static int tms5220_ready_r() {
        /* bring up to date first */
        stream_update(stream, -1);
        return tms5220_ready_read();
    }

    /**
     * ********************************************************************************************
     * tms5220_int_r -- return the int status from the sound chip
     * *********************************************************************************************
     */
    public static int tms5220_int_r() {
        /* bring up to date first */
        stream_update(stream, -1);
        return tms5220_int_read();
    }

    /**
     * ********************************************************************************************
     * tms5220_update -- update the sound chip so that it is in sync with CPU
     * execution
     * *********************************************************************************************
     */
    public static StreamInitPtr tms5220_update = new StreamInitPtr() {
        public void handler(int ch, ShortPtr buffer, int length) {
            ShortPtr sample_data = new ShortPtr(MAX_SAMPLE_CHUNK);
            ShortPtr curr_data = new ShortPtr(sample_data);
            short prev = last_sample, curr = curr_sample;
            /*UINT32*/
            int final_pos;
            /*UINT32*/
            int new_samples;


            /* finish off the current sample */
            if (source_pos > 0) {
                /* interpolate */
                while (length > 0 && source_pos < FRAC_ONE) {
                    buffer.writeinc((short) ((((int) prev * (FRAC_ONE - source_pos)) + ((int) curr * source_pos)) >> FRAC_BITS));
                    source_pos += source_step;
                    length--;
                }

                /* if we're over, continue; otherwise, we're done */
                if (source_pos >= FRAC_ONE) {
                    source_pos -= FRAC_ONE;
                } else {
                    tms5220_process(sample_data, 0);
                    return;
                }
            }

            /* compute how many new samples we need */
            final_pos = source_pos + length * source_step;
            new_samples = (final_pos + FRAC_ONE - 1) >> FRAC_BITS;
            if (new_samples > MAX_SAMPLE_CHUNK) {
                new_samples = MAX_SAMPLE_CHUNK;
            }

            /* generate them into our buffer */
            tms5220_process(sample_data, new_samples);
            prev = curr;
            curr = curr_data.readinc();

            /* then sample-rate convert with linear interpolation */
            while (length > 0) {
                /* interpolate */
                while (length > 0 && source_pos < FRAC_ONE) {
                    buffer.writeinc((short) ((((int) prev * (FRAC_ONE - source_pos)) + ((int) curr * source_pos)) >> FRAC_BITS));

                    source_pos += source_step;
                    length--;
                }

                /* if we're over, grab the next samples */
                if (source_pos >= FRAC_ONE) {
                    source_pos -= FRAC_ONE;
                    prev = curr;
                    curr = curr_data.readinc();
                }
            }

            /* remember the last samples */
            last_sample = prev;
            curr_sample = curr;
        }
    };

    /**
     * ********************************************************************************************
     * tms5220_set_frequency -- adjusts the playback frequency
     * *********************************************************************************************
     */
    public static void tms5220_set_frequency(int frequency) {
        /* skip if output frequency is zero */
        if (Machine.sample_rate == 0) {
            return;
        }

        /* update the stream and compute a new step size */
        if (stream != -1) {
            stream_update(stream, 0);
        }
        source_step = (/*UINT32*/int) ((double) (frequency / 80) * (double) FRAC_ONE / (double) Machine.sample_rate);
    }
}
