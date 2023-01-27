/*
 * ported to v0.36
 * using automatic conversion tool v0.01
 */
/**
 * Changelog
 * =========
 * 27/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.sndhrdw;

//driver imports
import static arcadeflex.v036.drivers.bzone.*;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.sndintrfH.*;
//sound imports
import static arcadeflex.v036.sound.streams.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.sound.pokey.*;

public class redbaron {

    /* Statics */
    static ShortPtr vol_lookup = null;

    static short[] vol_crash = new short[16];

    static int channel;
    static int latch;
    static int poly_counter;
    static int poly_shift;

    static int filter_counter;

    static int crash_amp;
    static int shot_amp;
    static int shot_amp_counter;

    static int squeal_amp;
    static int squeal_amp_counter;
    static int squeal_off_counter;
    static int squeal_on_counter;
    static int squeal_out;

    public static WriteHandlerPtr redbaron_sounds_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* If sound is off, don't bother playing samples */
            if (data == latch) {
                return;
            }

            stream_update(channel, 0);
            latch = data;
            rb_input_select = data & 1;
        }
    };

    public static WriteHandlerPtr redbaron_pokey_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((latch & 0x20) != 0) {
                pokey1_w.handler(offset, data);
            }
        }
    };
    public static StreamInitPtr redbaron_sound_update = new StreamInitPtr() {
        public void handler(int param, ShortPtr buffer, int length) {
            while (length-- != 0) {
                int sum = 0;

                /* polynome shifter E5 and F4 (LS164) clocked with 12kHz */
                poly_counter -= 12000;
                while (poly_counter <= 0) {
                    poly_counter += Machine.sample_rate;
                    if (((poly_shift & 0x0001) == 0) == ((poly_shift & 0x4000) == 0)) {
                        poly_shift = (poly_shift << 1) | 1;
                    } else {
                        poly_shift <<= 1;
                    }
                }

                /* What is the exact low pass filter frequency? */
                filter_counter -= 330;
                while (filter_counter <= 0) {
                    filter_counter += Machine.sample_rate;
                    crash_amp = (poly_shift & 1) != 0 ? latch >> 4 : 0;
                }
                /* mix crash sound at 35% */
                sum += vol_crash[crash_amp] * 35 / 100;

                /* shot not active: charge C32 (0.1u) */
                if ((latch & 0x04) == 0) {
                    shot_amp = 32767;
                } else if ((poly_shift & 0x8000) == 0) {
                    if (shot_amp > 0) {
                        /* discharge C32 (0.1u) through R26 (33k) + R27 (15k)
					 * 0.68 * C32 * (R26 + R27) = 3264us
                         */
                        //				#define C32_DISCHARGE_TIME (int)(32767 / 0.003264);
                        /* I think this is to short. Is C32 really 1u? */
                        //#define C32_DISCHARGE_TIME (int)(32767 / 0.03264);
                        shot_amp_counter -= (int) (32767 / 0.03264);
                        while (shot_amp_counter <= 0) {
                            shot_amp_counter += Machine.sample_rate;
                            if (--shot_amp == 0) {
                                break;
                            }
                        }
                        /* mix shot sound at 35% */
                        sum += vol_lookup.read(shot_amp) * 35 / 100;
                    }
                }

                if ((latch & 0x02) == 0) {
                    squeal_amp = 0;
                } else {
                    if (squeal_amp < 32767) {
                        /* charge C5 (22u) over R3 (68k) and CR1 (1N914)
					 * time = 0.68 * C5 * R3 = 1017280us
                         */
                        //#define C5_CHARGE_TIME (int)(32767 / 1.01728);
                        squeal_amp_counter -= (int) (32767 / 1.01728);
                        while (squeal_amp_counter <= 0) {
                            squeal_amp_counter += Machine.sample_rate;
                            if (++squeal_amp == 32767) {
                                break;
                            }
                        }
                    }

                    if (squeal_out != 0) {
                        /* NE555 setup as pulse position modulator
					 * C = 0.01u, Ra = 33k, Rb = 47k
					 * frequency = 1.44 / ((33k + 2*47k) * 0.01u) = 1134Hz
					 * modulated by squeal_amp
                         */
                        squeal_off_counter -= (1134 + 1134 * squeal_amp / 32767) / 3;
                        while (squeal_off_counter <= 0) {
                            squeal_off_counter += Machine.sample_rate;
                            squeal_out = 0;
                        }
                    } else {
                        squeal_on_counter -= 1134;
                        while (squeal_on_counter <= 0) {
                            squeal_on_counter += Machine.sample_rate;
                            squeal_out = 1;
                        }
                    }
                }

                /* mix sequal sound at 40% */
                if (squeal_out != 0) {
                    sum += 32767 * 40 / 100;
                }

                buffer.writeinc((short) sum);
            }
        }
    };

    public static ShStartHandlerPtr redbaron_sh_start = new ShStartHandlerPtr() {
        public int handler(MachineSound msound) {
            int i;

            vol_lookup = new ShortPtr(32768 * 2);
            if (vol_lookup == null) {
                return 1;
            }

            for (i = 0; i < 0x8000; i++) {
                vol_lookup.write(0x7fff - i, (short) (0x7fff / Math.exp(1.0 * i / 4096)));
            }

            for (i = 0; i < 16; i++) {
                /* r0 = R18 and R24, r1 = open */
                double r0 = 1.0 / (5600 + 680), r1 = 1 / 6e12;

                /* R14 */
                if ((i & 1) != 0) {
                    r1 += 1.0 / 8200;
                } else {
                    r0 += 1.0 / 8200;
                }
                /* R15 */
                if ((i & 2) != 0) {
                    r1 += 1.0 / 3900;
                } else {
                    r0 += 1.0 / 3900;
                }
                /* R16 */
                if ((i & 4) != 0) {
                    r1 += 1.0 / 2200;
                } else {
                    r0 += 1.0 / 2200;
                }
                /* R17 */
                if ((i & 8) != 0) {
                    r1 += 1.0 / 1000;
                } else {
                    r0 += 1.0 / 1000;
                }
                r0 = 1.0 / r0;
                r1 = 1.0 / r1;
                vol_crash[i] = (short) (32767 * r0 / (r0 + r1));
            }

            channel = stream_init("Custom", 50, Machine.sample_rate, 0, redbaron_sound_update);
            if (channel == -1) {
                return 1;
            }

            return 0;
        }
    };

    public static ShStopHandlerPtr redbaron_sh_stop = new ShStopHandlerPtr() {
        public void handler() {
            if (vol_lookup != null) {
                vol_lookup = null;
            }
        }
    };

    public static ShUpdateHandlerPtr redbaron_sh_update = new ShUpdateHandlerPtr() {
        public void handler() {
            stream_update(channel, 0);
        }
    };

}
