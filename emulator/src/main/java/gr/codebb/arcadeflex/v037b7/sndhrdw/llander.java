/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.sndhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.sound.mixer.*;

public class llander {

	/* Variables for the lander custom sound support */

    public static final int MIN_SLICE = 10;
    public static final int LANDER_OVERSAMPLE_RATE = 768000;

    public static int AUDIO_CONV16(int A) {
        return ((A) - 0x8000);
    }

    static int[] sinetable =
            {
                    128, 140, 153, 165, 177, 188, 199, 209, 218, 226, 234, 240, 245, 250, 253, 254,
                    255, 254, 253, 250, 245, 240, 234, 226, 218, 209, 199, 188, 177, 165, 153, 140,
                    128, 116, 103, 91, 79, 68, 57, 47, 38, 30, 22, 16, 11, 6, 3, 2,
                    1, 2, 3, 6, 11, 16, 22, 30, 38, 47, 57, 68, 79, 91, 103, 116
            };

    static int llander_volume[] = {0x00, 0x20, 0x40, 0x60, 0x80, 0xa0, 0xc0, 0xff};
    static int buffer_len;
    static int emulation_rate;
    static long multiplier;
    static int sample_pos;
    static int channel;
    static int lfsr_index;
    static ShortPtr sample_buffer;
    static char[] lfsr_buffer;

    static int volume;
    static int tone_6khz;
    static int tone_3khz;
    static int llander_explosion;

    public static ShStartHandlerPtr llander_sh_start = new ShStartHandlerPtr() {
        public int handler(MachineSound msound) {
            int loop, lfsrtmp, nor1, nor2, bit14, bit6;
            long fraction, remainder;
	
	        /* Dont initialise if no sound system */
            if (Machine.sample_rate == 0) return 0;
	
		/* Initialise the simple vars */

            volume = 0;
            tone_3khz = 0;
            tone_6khz = 0;
            llander_explosion = 0;

            buffer_len = (int) (Machine.sample_rate / Machine.drv.frames_per_second);
            emulation_rate = (int) (buffer_len * Machine.drv.frames_per_second);
            sample_pos = 0;
	
		/* Calculate the multipler to convert output sample number to the oversample rate (768khz) number */
		/* multipler is held as a fixed point number 16:16                                                */

            multiplier = LANDER_OVERSAMPLE_RATE / (long) emulation_rate;
            remainder = multiplier * LANDER_OVERSAMPLE_RATE;
            fraction = remainder << 16;
            fraction /= emulation_rate;

            multiplier = (multiplier << 16) + fraction;

            //	logerror("LANDER: Multiplier=%lx remainder=%lx fraction=%lx rate=%x\n",multiplier,remainder,fraction,emulation_rate);
	
		/* Generate the LFSR lookup table for the lander white noise generator */

            lfsr_index = 0;
            if ((lfsr_buffer = new char[65536 * 2]) == null) return 1;

            for (loop = 0; loop < 65536; loop++) {
			/* Calc next LFSR value from current value */

                lfsrtmp = (short) loop << 1;

                bit14 = (loop & 0x04000) != 0 ? 1 : 0;
                bit6 = (loop & 0x0040) != 0 ? 1 : 0;

                nor1 = (!(bit14 != 0 && bit6 != 0)) ? 0 : 1;			/* Note the inversion for the NOR gate */
                nor2 = (!(bit14 == 0 && bit6 == 0)) ? 0 : 1;
                lfsrtmp |= nor1 | nor2;

                lfsr_buffer[loop]= (char)lfsrtmp;

                //		logerror("LFSR Buffer: %04x    Next=%04x\n",loop, lfsr_buffer[loop]);
            }
	
		/* Allocate channel and buffer */

            channel = mixer_allocate_channel(25);

            if ((sample_buffer = new ShortPtr(2 * buffer_len)) == null) return 1;
            //memset(sample_buffer,0,sizeof(INT16)*buffer_len);

            return 0;
        }
    };

    public static ShStopHandlerPtr llander_sh_stop = new ShStopHandlerPtr() {
        public void handler() {
        }
    };


    /***************************************************************************
     * Sample Generation code.
     * <p>
     * Lander has 4 sound sources: 3khz, 6khz, thrust, explosion
     * <p>
     * As the filtering removes a lot of the signal amplitute on thrust and
     * explosion paths the gain is partitioned unequally:
     * <p>
     * 3khz (tone)             Gain 1
     * 6khz (tone)             Gain 1
     * thrust (12khz noise)    Gain 2
     * explosion (12khz noise) Gain 4
     * <p>
     * After combining the sources the output is scaled accordingly. (Div 8)
     * <p>
     * Sound generation is done by oversampling (x64) of the signal to remove the
     * need to interpolate between samples. It gives the closest point of the
     * sample point to the real signal, there is a small error between the two, the
     * higher the oversample rate the lower the error.
     * <p>
     * oversample_rate
     * oversample_number = sample_number * ---------------
     * sample_rate
     * <p>
     * e.g for sample rate=44100 hz and oversample_rate=768000 hz
     * <p>
     * oversample_number = sample_number * 17.41487
     * <p>
     * The calculations are all done in fixed point 16.16 format. The oversample is
     * mapped to the sinewave in the following manner
     * <p>
     * e.g for 3khz (12khz / 4)
     * <p>
     * sine point = ( oversample_number / 4 ) & 0b00111111
     * <p>
     * this coverts the oversample down to 3khz x 64 then wraps the buffer mod
     * 64 to give the sample point.
     * <p>
     * The oversample rate chosen in lander is 12khz * 64 = 768khz as 12khz is a
     * binary multiple of the all the frequencies involved.
     * <p>
     * The noise generation is done by linear feedback shift register which I've
     * modelled with an array, the array value at the current index points to the
     * next index to be used, the table is precalulated at startup.
     * <p>
     * The output of noise is taken evertime we cross a 12khz boundary, the code
     * then sets a target value (noisetarg), we then scan the gap between the last
     * oversample point and the current oversample point at the oversample rate
     * using the following algorithm:
     * <p>
     * noisecurrent = noisecurrent + (noisetarg-noisecurrent) * small_value
     * <p>
     * (currently small value = 1/256)
     * <p>
     * again this is done in fixed point 16.16 and results in the smoothing of the
     * output waveform which reduces the high frequency noise. It also reduces the
     * overall amplitude swing of the output, hence the gain partitioning.
     * <p>
     * You could probably argue that the oversample rate could be dropped without
     * any loss in quality and would recude the cpu load, but lander is hardly a cpu
     * hog.
     * <p>
     * The outputs from all of the above are then added and scaled up/down to a single
     * sample
     * <p>
     * K.Wilkins 13/5/98
     ***************************************************************************/
    static int sampnum = 0;
    static long noisetarg = 0, noisecurrent = 0;
    static long lastoversampnum = 0;

    public static void llander_process(ShortPtr buffer, int start, int n) {

        int loop, sample;
        long oversampnum, loop2;

        for (loop = 0; loop < n; loop++) {
            oversampnum = (long) (sampnum * multiplier) >> 16;

            //		logerror("LANDER: sampnum=%x oversampnum=%lx\n",sampnum, oversampnum);
	
			/* Pick up new noise target value whenever 12khz changes */

            if (lastoversampnum >> 6 != oversampnum >> 6) {
                lfsr_index = lfsr_buffer[lfsr_index];
                noisetarg = (lfsr_buffer[lfsr_index] & 0x4000) != 0 ? llander_volume[volume] : 0x00;
                noisetarg <<= 16;
            }
	
			/* Do tracking of noisetarg to noise current done in fixed point 16:16    */
			/* each step takes us 1/256 of the difference between desired and current */

            for (loop2 = lastoversampnum; loop2 < oversampnum; loop2++) {
                noisecurrent += (noisetarg - noisecurrent) >> 7;	/* Equiv of multiply by 1/256 */
            }

            sample = (int) (noisecurrent >> 16);
            sample <<= 1;	/* Gain = 2 */

            if (tone_3khz != 0) {
                sample += sinetable[(int) ((oversampnum >> 2) & 0x3f)];
            }
            if (tone_6khz != 0) {
                sample += sinetable[(int) ((oversampnum >> 1) & 0x3f)];
            }
            if (llander_explosion != 0) {
                sample += (int) (noisecurrent >> (16 - 2));	/* Gain of 4 */
            }
	
			/* Scale ouput down to buffer */

            buffer.write(start + loop, (short) AUDIO_CONV16(sample << 5));

            sampnum++;
            lastoversampnum = oversampnum;
        }
    }


    public static ShUpdateHandlerPtr llander_sh_update_partial = new ShUpdateHandlerPtr() {
        public void handler() {
            int newpos;

            if (Machine.sample_rate == 0) return;

            newpos = sound_scalebufferpos(buffer_len); /* get current position based on the timer */

            if (newpos - sample_pos < MIN_SLICE) return;
	
		/* Process count samples into the buffer */

            llander_process(sample_buffer, sample_pos, newpos - sample_pos);
	
		/* Update sample position */

            sample_pos = newpos;
        }
    };


    public static ShUpdateHandlerPtr llander_sh_update = new ShUpdateHandlerPtr() {
        public void handler() {
            if (Machine.sample_rate == 0) return;

            if (sample_pos < buffer_len)
                llander_process(sample_buffer, sample_pos, buffer_len - sample_pos);
            sample_pos = 0;

            mixer_play_streamed_sample_16(channel, sample_buffer, 2 * buffer_len, emulation_rate);
        }
    };

    public static WriteHandlerPtr llander_snd_reset_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            lfsr_index = 0;
        }
    };

    public static WriteHandlerPtr llander_sounds_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
		/* Update sound to present */
            llander_sh_update_partial.handler();
	
		/* Lunar Lander sound breakdown */

            volume = data & 0x07;
            tone_3khz = data & 0x10;
            tone_6khz = data & 0x20;
            llander_explosion = data & 0x08;
        }
    };

}
