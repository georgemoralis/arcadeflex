/*
 * ported to v0.36
 * 
 */
package arcadeflex.v036.sound;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import gr.codebb.arcadeflex.v036.mame.sndintrf;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.sound.sn76496H.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class sn76496 extends snd_interface {

    private static final int MAX_OUTPUT = 0x7fff;
    private static final int STEP = 0x10000;/* noise feedback for white noise mode */
    private static final int FB_WNOISE = 0x12000;/* bit15.d(16bits) = bit0(out) ^ bit2 */

    private static final int FB_PNOISE = 0x08000;
    /* JH 981127 - fixes Do Run Run */
 /* noise generator start preset (for periodic noise) */

    private static final int NG_PRESET = 0x0f35;

    public static class _SN76496 {

        public _SN76496() {
            VolTable = new int[16];
            Register = new int[8];
            Volume = new int[4];
            Period = new int[4];
            Count = new int[4];
            Output = new int[4];
            Channel = 0;
            SampleRate = 0;
            LastRegister = 0;
            UpdateStep = 0;
            RNG = 0;
            NoiseFB = 0;
        }

        int Channel;
        int SampleRate;
        int UpdateStep;
        int[] VolTable;/* volume table         */

        int[] Register;/* registers */

        int LastRegister;/* last register written */
        int[] Volume;/* volume of voice 0-2 and noise */
        int RNG;/* noise generator      */
        int NoiseFB;/* noise feedback mask */

        int[] Period;
        int[] Count;
        int[] Output;
    }

    public static _SN76496[] sn = new _SN76496[MAX_76496];

    public sn76496() {
        sound_num = SOUND_SN76496;
        name = "SN76496";
        for (int i = 0; i < MAX_76496; i++) {
            sn[i] = new _SN76496();
        }
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((SN76496interface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((SN76496interface) msound.sound_interface).baseclock[0];
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

    public static void SN76496Write(int chip, int data) {
        _SN76496 R = sn[chip];


        /* update the output buffer before changing the registers */
        stream_update(R.Channel, 0);

        if ((data & 0x80) != 0) {
            int r = (data & 0x70) >> 4;
            int c = (int) r / 2;

            R.LastRegister = r;
            R.Register[r] = (R.Register[r] & 0x3f0) | (data & 0x0f);
            switch (r) {
                case 0:
                /* tone 0 : frequency */

                case 2:
                /* tone 1 : frequency */

                case 4:
                    /* tone 2 : frequency */

                    R.Period[c] = R.UpdateStep * R.Register[r];
                    if (R.Period[c] == 0) {
                        R.Period[c] = R.UpdateStep;
                    }
                    if (r == 4) {
                        /* update noise shift frequency */
                        if ((R.Register[6] & 0x03) == 0x03) {
                            R.Period[3] = 2 * R.Period[2];
                        }
                    }
                    break;
                case 1:
                /* tone 0 : volume */

                case 3:
                /* tone 1 : volume */

                case 5:
                /* tone 2 : volume */

                case 7:
                    /* noise  : volume */

                    R.Volume[c] = R.VolTable[data & 0x0f];
                    break;
                case 6: /* noise  : frequency, mode */ {
                    int n = R.Register[6];
                    R.NoiseFB = ((n & 4) != 0) ? FB_WNOISE : FB_PNOISE;
                    n &= 3;
                    /* N/512,N/1024,N/2048,Tone #3 output */
                    R.Period[3] = (n == 3) ? 2 * R.Period[2] : (R.UpdateStep << (5 + n));

                    /* reset noise shifter */
                    R.RNG = NG_PRESET;
                    R.Output[3] = R.RNG & 1;
                }
                break;
            }
        } else {
            int r = R.LastRegister;
            int c = r / 2;

            switch (r) {
                case 0:
                /* tone 0 : frequency */

                case 2:
                /* tone 1 : frequency */

                case 4:
                    /* tone 2 : frequency */

                    R.Register[r] = (R.Register[r] & 0x0f) | ((data & 0x3f) << 4);
                    R.Period[c] = R.UpdateStep * R.Register[r];
                    if (R.Period[c] == 0) {
                        R.Period[c] = R.UpdateStep;
                    }
                    if (r == 4) {
                        /* update noise shift frequency */
                        if ((R.Register[6] & 0x03) == 0x03) {
                            R.Period[3] = 2 * R.Period[2];
                        }
                    }
                    break;
            }
        }
    }

    public static WriteHandlerPtr SN76496_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            SN76496Write(0, data);
        }
    };
    public static WriteHandlerPtr SN76496_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            SN76496Write(1, data);
        }
    };
    public static WriteHandlerPtr SN76496_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            SN76496Write(2, data);
        }
    };
    public static WriteHandlerPtr SN76496_3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            SN76496Write(3, data);
        }
    };

    public static StreamInitPtr SN76496Update = new StreamInitPtr() {
        public void handler(int chip, ShortPtr buffer, int length) {
            int i;
            _SN76496 R = sn[chip];

            /* If the volume is 0, increase the counter */
            for (i = 0; i < 4; i++) {
                if (R.Volume[i] == 0) {
                    /* note that I do count += length, NOT count = length + 1. You might think */
 /* it's the same since the volume is 0, but doing the latter could cause */
 /* interferencies when the program is rapidly modulating the volume. */
                    if (R.Count[i] <= length * STEP) {
                        R.Count[i] += length * STEP;
                    }
                }
            }
            while (length > 0) {
                int[] vol = new int[4];
                /*unsigned*/ int out;
                int left;


                /* vol[] keeps track of how long each square wave stays */
 /* in the 1 position during the sample period. */
                vol[0] = vol[1] = vol[2] = vol[3] = 0;

                for (i = 0; i < 3; i++) {
                    if (R.Output[i] != 0) {
                        vol[i] += R.Count[i];
                    }
                    R.Count[i] -= STEP;
                    /* Period[i] is the half period of the square wave. Here, in each */
 /* loop I add Period[i] twice, so that at the end of the loop the */
 /* square wave is in the same status (0 or 1) it was at the start. */
 /* vol[i] is also incremented by Period[i], since the wave has been 1 */
 /* exactly half of the time, regardless of the initial position. */
 /* If we exit the loop in the middle, Output[i] has to be inverted */
 /* and vol[i] incremented only if the exit status of the square */
 /* wave is 1. */
                    while (R.Count[i] <= 0) {
                        R.Count[i] += R.Period[i];
                        if (R.Count[i] > 0) {
                            R.Output[i] ^= 1;
                            if (R.Output[i] != 0) {
                                vol[i] += R.Period[i];
                            }
                            break;
                        }
                        R.Count[i] += R.Period[i];
                        vol[i] += R.Period[i];
                    }
                    if (R.Output[i] != 0) {
                        vol[i] -= R.Count[i];
                    }
                }

                left = STEP;
                do {
                    int nextevent;

                    if (R.Count[3] < left) {
                        nextevent = R.Count[3];
                    } else {
                        nextevent = left;
                    }

                    if (R.Output[3] != 0) {
                        vol[3] += R.Count[3];
                    }
                    R.Count[3] -= nextevent;
                    if (R.Count[3] <= 0) {
                        if ((R.RNG & 1) != 0) {
                            R.RNG ^= R.NoiseFB;
                        }
                        R.RNG >>= 1;
                        R.Output[3] = R.RNG & 1;
                        R.Count[3] += R.Period[3];
                        if (R.Output[3] != 0) {
                            vol[3] += R.Period[3];
                        }
                    }
                    if (R.Output[3] != 0) {
                        vol[3] -= R.Count[3];
                    }

                    left -= nextevent;
                } while (left > 0);

                out = vol[0] * R.Volume[0] + vol[1] * R.Volume[1]
                        + vol[2] * R.Volume[2] + vol[3] * R.Volume[3];

                if (out > MAX_OUTPUT * STEP) {
                    out = MAX_OUTPUT * STEP;
                }

                buffer.write(0, (short) (out / STEP));
                buffer.offset += 2;
                length--;
            }
        }
    };

    static void SN76496_set_clock(int chip, int clock) {
        _SN76496 R = sn[chip];
        /* the base clock for the tone generators is the chip clock divided by 16; */
 /* for the noise generator, it is clock / 256. */
 /* Here we calculate the number of steps which happen during one sample */
 /* at the given sample rate. No. of events = sample rate / (clock/16). */
 /* STEP is a multiplier used to turn the fraction into a fixed point */
 /* number. */
        R.UpdateStep = (int) (((double) STEP * R.SampleRate * 16) / clock);
    }

    static void SN76496_set_gain(int chip, int gain) {
        _SN76496 R = sn[chip];
        int i;
        double out;

        gain &= 0xff;

        /* increase max output basing on gain (0.2 dB per step) */
        out = MAX_OUTPUT / 3;
        while (gain-- > 0) {
            out *= 1.023292992;
            /* = (10 ^ (0.2/20)) */

        }

        /* build volume table (2dB per step) */
        for (i = 0; i < 15; i++) {
            /* limit volume to avoid clipping */
            if (out > MAX_OUTPUT / 3) {
                R.VolTable[i] = MAX_OUTPUT / 3;
            } else {
                R.VolTable[i] = (int) out;
            }

            out /= 1.258925412;
            /* = 10 ^ (2/20) = 2dB */

        }
        R.VolTable[15] = 0;
    }

    static int SN76496_init(MachineSound msound, int chip, int clock, int volume, int sample_rate) {
        int i;
        _SN76496 R = sn[chip];
        String name;

        name = sprintf("SN76496 #%d", chip);
        R.Channel = stream_init(name, volume, sample_rate, chip, SN76496Update);

        if (R.Channel == -1) {
            return 1;
        }

        R.SampleRate = sample_rate;
        SN76496_set_clock(chip, clock);

        for (i = 0; i < 4; i++) {
            R.Volume[i] = 0;
        }

        R.LastRegister = 0;
        for (i = 0; i < 8; i += 2) {
            R.Register[i] = 0;
            R.Register[i + 1] = 0x0f;
            /* volume = 0 */

        }

        for (i = 0; i < 4; i++) {
            R.Output[i] = 0;
            R.Period[i] = R.Count[i] = (int) R.UpdateStep;
        }
        R.RNG = NG_PRESET;
        R.Output[3] = (int) R.RNG & 1;

        return 0;
    }

    @Override
    public int start(MachineSound msound) {

        int chip;
        SN76496interface intf = (SN76496interface) msound.sound_interface;

        for (chip = 0; chip < intf.num; chip++) {
            if (SN76496_init(msound, chip, intf.baseclock[chip], intf.volume[chip] & 0xff, Machine.sample_rate) != 0) {
                return 1;
            }

            SN76496_set_gain(chip, (intf.volume[chip] >> 8) & 0xff);
        }
        return 0;
    }
}
