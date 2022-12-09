/*
 * ported to v0.36
 * using automatic conversion tool v0.01
 */
package arcadeflex.v036.sndhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.sound.streams.*;
import static arcadeflex.v036.sound.tms36xx.*;

public class phoenix {

    static int VMIN = 0;
    static int VMAX = 32767;

    static int sound_latch_a;
    static int sound_latch_b;

    static int channel;

    static int tone1_vco1_cap;
    static int tone1_level;
    static int tone2_level;

    static /*UINT32*/ int[] poly18 = null;
    public static final double C18a = 0.01e-6;
    public static final double C18b = 0.48e-6;
    public static final double C18c = 1.01e-6;
    public static final double C18d = 1.48e-6;
    public static final double R40 = 47000;
    public static final double R41 = 100000;
    static int rate[][] = {
        {
            (int) (VMAX * 2 / 3 / (0.693 * (R40 + R41) * C18a)),
            (int) (VMAX * 2 / 3 / (0.693 * (R40 + R41) * C18b)),
            (int) (VMAX * 2 / 3 / (0.693 * (R40 + R41) * C18c)),
            (int) (VMAX * 2 / 3 / (0.693 * (R40 + R41) * C18d))
        },
        {
            (int) (VMAX * 2 / 3 / (0.693 * R41 * C18a)),
            (int) (VMAX * 2 / 3 / (0.693 * R41 * C18b)),
            (int) (VMAX * 2 / 3 / (0.693 * R41 * C18c)),
            (int) (VMAX * 2 / 3 / (0.693 * R41 * C18d))
        }
    };
    static int output_vco1, counter_vco1, level_vco1;

    static int tone1_vco1(int samplerate) {

        if (output_vco1 != 0) {
            if (level_vco1 > VMAX * 1 / 3) {
                counter_vco1 -= rate[1][tone1_vco1_cap];
                if (counter_vco1 <= 0) {
                    int steps = -counter_vco1 / samplerate + 1;
                    counter_vco1 += steps * samplerate;
                    if ((level_vco1 -= steps) <= VMAX * 1 / 3) {
                        level_vco1 = VMAX * 1 / 3;
                        output_vco1 = 0;
                    }
                }
            }
        } else {
            if (level_vco1 < VMAX * 2 / 3) {
                counter_vco1 -= rate[0][tone1_vco1_cap];
                if (counter_vco1 <= 0) {
                    int steps = -counter_vco1 / samplerate + 1;
                    counter_vco1 += steps * samplerate;
                    if ((level_vco1 += steps) >= VMAX * 2 / 3) {
                        level_vco1 = VMAX * 2 / 3;
                        output_vco1 = 1;
                    }
                }
            }
        }
        return output_vco1;
    }
    public static final double C20 = 10.0e-6;
    public static final double R43 = 570000;
    public static final double R44 = 570000;

    static int output_vco2, counter_vco2, level_vco2;

    static int tone1_vco2(int samplerate) {
        if (output_vco2 != 0) {
            if (level_vco2 > VMIN) {
                counter_vco2 -= (int) (VMAX * 2 / 3 / (0.693 * R44 * C20));
                if (counter_vco2 <= 0) {
                    int steps = -counter_vco2 / samplerate + 1;
                    counter_vco2 += steps * samplerate;
                    if ((level_vco2 -= steps) <= VMAX * 1 / 3) {
                        level_vco2 = VMAX * 1 / 3;
                        output_vco2 = 0;
                    }
                }
            }
        } else {
            if (level_vco2 < VMAX) {
                counter_vco2 -= (int) (VMAX * 2 / 3 / (0.693 * (R43 + R44) * C20));
                if (counter_vco2 <= 0) {
                    int steps = -counter_vco2 / samplerate + 1;
                    counter_vco2 += steps * samplerate;
                    if ((level_vco2 += steps) >= VMAX * 2 / 3) {
                        level_vco2 = VMAX * 2 / 3;
                        output_vco2 = 1;
                    }
                }
            }
        }

        return output_vco2;
    }
    public static final double C22 = 100.0e-6;
    public static final double R42 = 10000;
    public static final double R45 = 51000;
    public static final double R46 = 51000;
    public static final double RP = 27777;/* R42+R46 parallel with R45 */
    static int counter_vco, level_vco, rate_vco, charge_vco;

    static int tone1_vco(int samplerate_vco, int vco1, int vco2) {

        int voltage;

        if (level_vco != charge_vco) {
            /* charge_vco or discharge_vco C22 */
            counter_vco -= rate_vco;
            while (counter_vco <= 0) {
                counter_vco += samplerate_vco;
                if (level_vco < charge_vco) {
                    if (++level_vco == charge_vco) {
                        break;
                    }
                } else {
                    if (--level_vco == charge_vco) {
                        break;
                    }
                }
            }
        }

        if (vco2 != 0) {

            if (vco1 != 0) {
                /*		R42 10k
				 * +5V -/\/\/------------+
				 *			   5V		 |
				 * +5V -/\/\/--+--/\/\/--+-. V/C
				 *	   R45 51k | R46 51k
				 *			  ---
				 *			  --- 100u
				 *			   |
				 *			  0V
                 */
                charge_vco = VMAX;
                rate_vco = (int) ((charge_vco - level_vco) / (RP * C22));
                voltage = (int) (level_vco + (VMAX - level_vco) * R46 / (R46 + R42));
            } else {
                /*		R42 10k
				 *	0V -/\/\/------------+
				 *			  2.7V		 |
				 * +5V -/\/\/--+--/\/\/--+-. V/C
				 *	   R45 51k | R46 51k
				 *			  ---
				 *			  --- 100u
				 *			   |
				 *			  0V
                 */
 /* simplification: charge_vco = (R42 + R46) / (R42 + R45 + R46); */
                charge_vco = VMAX * 27 / 50;
                if (charge_vco >= level_vco) {
                    rate_vco = (int) ((charge_vco - level_vco) / (R45 * C22));
                } else {
                    rate_vco = (int) ((level_vco - charge_vco) / ((R46 + R42) * C22));
                }
                voltage = (int) (level_vco * R42 / (R46 + R42));
            }
        } else {
            if (vco1 != 0) {
                /*		R42 10k
				 * +5V -/\/\/------------+
				 *			  2.3V		 |
				 *	0V -/\/\/--+--/\/\/--+-. V/C
				 *	   R45 51k | R46 51k
				 *			  ---
				 *			  --- 100u
				 *			   |
				 *			  0V
                 */
 /* simplification: charge_vco = VMAX * R45 / (R42 + R45 + R46); */
                charge_vco = VMAX * 23 / 50;
                if (charge_vco >= level_vco) {
                    rate_vco = (int) ((charge_vco - level_vco) / ((R42 + R46) * C22));
                } else {
                    rate_vco = (int) ((level_vco - charge_vco) / (R45 * C22));
                }
                voltage = (int) (level_vco + (VMAX - level_vco) * R46 / (R42 + R46));
            } else {
                /*		R42 10k
				 *	0V -/\/\/------------+
				 *			   0V		 |
				 *	0V -/\/\/--+--/\/\/--+-. V/C
				 *	   R45 51k | R46 51k
				 *			  ---
				 *			  --- 100u
				 *			   |
				 *			  0V
                 */
                charge_vco = VMIN;
                rate_vco = (int) ((level_vco - charge_vco) / (RP * C22));
                voltage = (int) (level_vco * R42 / (R46 + R42));
            }
        }

        /* L507 (NE555): Ra=20k, Rb=20k, C=0.001uF
		 * frequency 1.44/((Ra+2*Rb)*C) = 24kHz
         */
        return 24000 * 1 / 3 + 24000 * 2 / 3 * voltage / 32768;
    }
    static int counter_tone1, divisor_tone1, output_tone1;

    static int tone1(int samplerate) {
        int vco1 = tone1_vco1(samplerate);
        int vco2 = tone1_vco2(samplerate);
        int frequency = tone1_vco(samplerate, vco1, vco2);

        if ((sound_latch_a & 15) != 15) {
            counter_tone1 -= frequency;
            while (counter_tone1 <= 0) {
                counter_tone1 += samplerate;
                if (++divisor_tone1 == 16) {
                    divisor_tone1 = sound_latch_a & 15;
                    output_tone1 ^= 1;
                }
            }
        }

        return output_tone1 != 0 ? tone1_level : -tone1_level;
    }
    public static final double C7 = 6.8e-6;
    public static final double R23 = 100000;
    public static final double R22 = 47000;
    public static final double R24 = 33000;
    public static final double R22pR24 = 19388;

    public static final double C7_MIN = (VMAX * 254 / 500);
    public static final double C7_MAX = (VMAX * 551 / 500);
    public static final double C7_DIFF = (C7_MAX - C7_MIN);
    static int counter_t2vco, level_t2vco;

    static int tone2_vco(int samplerate) {

        if ((sound_latch_b & 0x10) == 0) {
            counter_t2vco -= (C7_MAX - level_t2vco) * 12 / (R23 * C7) / 5;
            if (counter_t2vco <= 0) {
                int n = (-counter_t2vco / samplerate) + 1;
                counter_t2vco += n * samplerate;
                if ((level_t2vco += n) > C7_MAX) {
                    level_t2vco = (int) C7_MAX;
                }
            }
        } else {
            counter_t2vco -= (level_t2vco - C7_MIN) * 12 / (R22pR24 * C7) / 5;
            if (counter_t2vco <= 0) {
                int n = (-counter_t2vco / samplerate) + 1;
                counter_t2vco += n * samplerate;
                if ((level_t2vco -= n) < C7_MIN) {
                    level_t2vco = (int) C7_MIN;
                }
            }
        }
        /*
		 * L487 (NE555):
		 * Ra = R25 (47k), Rb = R26 (47k), C = C8 (0.001uF)
		 * frequency 1.44/((Ra+2*Rb)*C) = 10212 Hz
         */
        return 10212 * level_t2vco / 32768;
    }
    static int counter_tone2, divisor_tone2, output_tone2;

    static int tone2(int samplerate) {
        int frequency = tone2_vco(samplerate);

        if ((sound_latch_b & 15) != 15) {
            counter_tone2 -= frequency;
            while (counter_tone2 <= 0) {
                counter_tone2 += samplerate;
                if (++divisor_tone2 == 16) {
                    divisor_tone2 = sound_latch_b & 15;
                    output_tone2 ^= 1;
                }
            }
        }
        return output_tone2 != 0 ? tone2_level : -tone2_level;
    }
    public static final double C24 = 6.8e-6;
    public static final double R49 = 1000;
    public static final double R51 = 330;
    public static final double R52 = 20000;
    static int counter_c24, level_c24;

    static int update_c24(int samplerate) {

        /*
		 * Noise frequency control (Port B):
		 * Bit 6 lo charges C24 (6.8u) via R51 (330) and when
		 * bit 6 is hi, C24 is discharged through R52 (20k)
		 * in approx. 20000 * 6.8e-6 = 0.136 seconds
         */
        if ((sound_latch_a & 0x40) != 0) {
            if (level_c24 > VMIN) {
                counter_c24 -= (int) ((level_c24 - VMIN) / (R52 * C24));
                if (counter_c24 <= 0) {
                    int n = -counter_c24 / samplerate + 1;
                    counter_c24 += n * samplerate;
                    if ((level_c24 -= n) < VMIN) {
                        level_c24 = VMIN;
                    }
                }
            }
        } else {
            if (level_c24 < VMAX) {
                counter_c24 -= (int) ((VMAX - level_c24) / ((R51 + R49) * C24));
                if (counter_c24 <= 0) {
                    int n = -counter_c24 / samplerate + 1;
                    counter_c24 += n * samplerate;
                    if ((level_c24 += n) > VMAX) {
                        level_c24 = VMAX;
                    }
                }
            }
        }
        return VMAX - level_c24;
    }
    public static final double C25 = 6.8e-6;
    public static final double R50 = 1000;
    public static final double R53 = 330;
    public static final double R54 = 47000;
    static int counter_c25, level_c25;

    static int update_c25(int samplerate) {

        if ((sound_latch_a & 0x80) != 0) {
            if (level_c25 < VMAX) {
                counter_c25 -= (int) ((VMAX - level_c25) / ((R50 + R53) * C25));
                if (counter_c25 <= 0) {
                    int n = -counter_c25 / samplerate + 1;
                    counter_c25 += n * samplerate;
                    if ((level_c25 += n) > VMAX) {
                        level_c25 = VMAX;
                    }
                }
            }
        } else {
            if (level_c25 > VMIN) {
                counter_c25 -= (int) ((level_c25 - VMIN) / (R54 * C25));
                if (counter_c25 <= 0) {
                    int n = -counter_c25 / samplerate + 1;
                    counter_c25 += n * samplerate;
                    if ((level_c25 -= n) < VMIN) {
                        level_c25 = VMIN;
                    }
                }
            }
        }
        return level_c25;
    }

    static int counter_n, polyoffs, polybit, lowpass_counter, lowpass_polybit;

    static int noise(int samplerate) {
        int vc24 = update_c24(samplerate);
        int vc25 = update_c25(samplerate);
        int sum = 0, level, frequency;

        /*
		 * The voltage levels are added and control I(CE) of transistor TR1
		 * (NPN) which then controls the noise clock frequency (linearily?).
		 * level = voltage at the output of the op-amp controlling the noise rate.
         */
        if (vc24 < vc25) {
            level = vc24 + (vc25 - vc24) / 2;
        } else {
            level = vc25 + (vc24 - vc25) / 2;
        }

        frequency = 588 + 6325 * level / 32768;

        /*
		 * NE555: Ra=47k, Rb=1k, C=0.05uF
		 * minfreq = 1.44 / ((47000+2*1000) * 0.05e-6) = approx. 588 Hz
		 * R71 (2700 Ohms) parallel to R73 (47k Ohms) = approx. 2553 Ohms
		 * maxfreq = 1.44 / ((2553+2*1000) * 0.05e-6) = approx. 6325 Hz
         */
        counter_n -= frequency;
        if (counter_n <= 0) {
            int n = (-counter_n / samplerate) + 1;
            counter_n += n * samplerate;
            polyoffs = (polyoffs + n) & 0x3ffff;
            polybit = (poly18[polyoffs >> 5] >> (polyoffs & 31)) & 1;
        }
        if (polybit == 0) {
            sum += vc24;
        }

        /* 400Hz crude low pass filter: this is only a guess!! */
        lowpass_counter -= 400;
        if (lowpass_counter <= 0) {
            lowpass_counter += samplerate;
            lowpass_polybit = polybit;
        }
        if (lowpass_polybit == 0) {
            sum += vc25;
        }

        return sum;
    }
    public static StreamInitPtr phoenix_sound_update = new StreamInitPtr() {
        public void handler(int param, ShortPtr buffer, int length) {
            int samplerate = Machine.sample_rate;

            while (length-- > 0) {
                int sum = 0;
                sum = (tone1(samplerate) + tone2(samplerate) + noise(samplerate)) / 4;
                buffer.writeinc(sum < 32768 ? sum > -32768 ? (short) sum : -32768 : 32767);
            }
        }
    };

    public static WriteHandlerPtr phoenix_sound_control_a_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (data == sound_latch_a) {
                return;
            }

            stream_update(channel, 0);
            sound_latch_a = data;

            tone1_vco1_cap = (sound_latch_a >> 4) & 3;
            if ((sound_latch_a & 0x20) != 0) {
                tone1_level = VMAX * 10000 / (10000 + 10000);
            } else {
                tone1_level = VMAX;
            }
        }
    };

    public static WriteHandlerPtr phoenix_sound_control_b_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (data == sound_latch_b) {
                return;
            }

            stream_update(channel, 0);
            sound_latch_b = data;

            if ((sound_latch_b & 0x20) != 0) {
                tone2_level = VMAX * 10 / 11;
            } else {
                tone2_level = VMAX;
            }

            /* eventually change the tune that the MM6221AA is playing */
            mm6221aa_tune_w(0, sound_latch_b >> 6);
        }
    };

    public static ShStartHandlerPtr phoenix_sh_start = new ShStartHandlerPtr() {
        public int handler(MachineSound msound) {
            int i, j;
            /*UINT32*/
            int shiftreg;

            poly18 = new int[(1 << (18 - 5)) * 4];

            if (poly18 == null) {
                return 1;
            }

            shiftreg = 0;
            for (i = 0; i < (1 << (18 - 5)); i++) {
                /*UINT32*/
                int bits = 0;
                for (j = 0; j < 32; j++) {
                    bits = (bits >> 1) | (shiftreg << 31);
                    if (((shiftreg >> 16) & 1) == ((shiftreg >> 17) & 1)) {
                        shiftreg = (shiftreg << 1) | 1;
                    } else {
                        shiftreg <<= 1;
                    }
                }
                poly18[i] = bits;
            }

            channel = stream_init("Custom", 50, Machine.sample_rate, 0, phoenix_sound_update);
            if (channel == -1) {
                return 1;
            }

            return 0;
        }
    };

    public static ShStopHandlerPtr phoenix_sh_stop = new ShStopHandlerPtr() {
        public void handler() {
            if (poly18 != null) {
                poly18 = null;
            }
        }
    };

    public static ShUpdateHandlerPtr phoenix_sh_update = new ShUpdateHandlerPtr() {
        public void handler() {
            stream_update(channel, 0);
        }
    };

}
