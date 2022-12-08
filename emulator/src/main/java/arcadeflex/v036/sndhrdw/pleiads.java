/*
 * ported to v0.36
 * 
 */
package arcadeflex.v036.sndhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.sndintrfH.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.mame.errorlog;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fprintf;
import static arcadeflex.v036.sound.streams.*;
import static arcadeflex.v036.sound.tms36xx.*;

public class pleiads {

    static int VMIN = 0;
    static int VMAX = 32767;

    static int channel;

    static int sound_latch_a;
    static int sound_latch_b;
    static int sound_latch_c;
    /* part of the videoreg_w latch */

    static /*UINT32*/ int[] poly18 = null;
    static int polybit;

    /* fixed 8kHz clock */
    static int TONE1_CLOCK = 8000;

    /* some resistor and capacitor dependent values which
	   vary between the (otherwise identical) boards. */
    static double pa5_charge_time;
    static double pa5_discharge_time;

    static double pa6_charge_time;
    static double pa6_discharge_time;

    static double pb4_charge_time;
    static double pb4_discharge_time;

    static double pc4_charge_time;
    static double pc4_discharge_time;

    static double pc5_charge_time;
    static double pc5_discharge_time;

    static int pa5_resistor;
    static int pc5_resistor;

    static int tone2_max_freq;
    static int tone3_max_freq;
    static int tone4_max_freq;
    static int noise_freq;
    static int polybit_resistor;
    static int opamp_resistor;

    /**
     * ***************************************************************************
     * Tone #1 is a fixed 8 kHz signal divided by 1 to 15.
     * ***************************************************************************
     */
    static int counter_tone1, divisor_tone1, output_tone1;

    static int tone1(int samplerate) {
        if ((sound_latch_a & 15) != 15) {
            counter_tone1 -= TONE1_CLOCK;
            while (counter_tone1 <= 0) {
                counter_tone1 += samplerate;
                if (++divisor_tone1 == 16) {
                    divisor_tone1 = sound_latch_a & 15;
                    output_tone1 ^= 1;
                }
            }
        }
        return output_tone1 != 0 ? VMAX : -VMAX;
    }

    /**
     * ***************************************************************************
     * Tones #2 and #3 are coming from the upper 556 chip It's labelled IC96 in
     * Pop Flamer, 4D(??) in Naughty Boy. C68 controls the frequencies of tones
     * #2 and #3 (V/C inputs)
     * ***************************************************************************
     */
    static int counter_pb4, level_pb4;

    static int update_pb4(int samplerate) {
        /* bit 4 of latch B: charge 10uF (C28/C68) through 10k (R19/R25) */
        if ((sound_latch_b & 0x10) != 0) {
            if (level_pb4 < VMAX) {
                counter_pb4 -= (int) ((VMAX - level_pb4) / pb4_charge_time);
                if (counter_pb4 <= 0) {
                    int n = (-counter_pb4 / samplerate) + 1;
                    counter_pb4 += n * samplerate;
                    if ((level_pb4 += n) > VMAX) {
                        level_pb4 = VMAX;
                    }
                }
            }
        } else {
            if (level_pb4 > VMIN) {
                counter_pb4 -= (int) ((level_pb4 - VMIN) / pb4_discharge_time);
                if (counter_pb4 <= 0) {
                    int n = (-counter_pb4 / samplerate) + 1;
                    counter_pb4 += n * samplerate;
                    if ((level_pb4 -= n) < VMIN) {
                        level_pb4 = VMIN;
                    }
                }
            }
        }
        return level_pb4;
    }
    static int counter2_tone23, output2_tone23, counter3_tone23, output3_tone23;

    static int tone23(int samplerate) {
        int level = VMAX - update_pb4(samplerate);
        int sum = 0;

        /* bit 5 = low: tone23 disabled */
        if ((sound_latch_b & 0x20) == 0) {
            return sum;
        }

        /* modulate timers from the upper 556 with the voltage on Cxx on PB4. */
        if (level < VMAX) {
            counter2_tone23 -= tone2_max_freq * level / 32768;
            if (counter2_tone23 <= 0) {
                int n = (-counter2_tone23 / samplerate) + 1;
                counter2_tone23 += n * samplerate;
                output2_tone23 = (output2_tone23 + n) & 1;
            }

            counter3_tone23 -= tone3_max_freq * 1 / 3 + tone3_max_freq * 2 / 3 * level / 33768;
            if (counter3_tone23 <= 0) {
                int n = (-counter2_tone23 / samplerate) + 1;
                counter3_tone23 += samplerate;
                output3_tone23 = (output3_tone23 + n) & 1;
            }
        }

        sum += (output2_tone23) != 0 ? VMAX : -VMAX;
        sum += (output3_tone23) != 0 ? VMAX : -VMAX;

        return sum / 2;
    }

    /**
     * ***************************************************************************
     * Tone #4 comes from upper half of the lower 556 (IC98 in Pop Flamer) It's
     * modulated by the voltage at C49, which is then divided between 0V or 5V,
     * depending on the polynome output bit. The tone signal gates two signals
     * (bits 5 of latches A and C), but these are also swept between two levels
     * (C52 and C53 in Pop Flamer).
     * ***************************************************************************
     */
    public static int PC4_MIN() {
        return (int) (VMAX * 7 / 50);
    }
    static int counter_pc4, level_pc4 = PC4_MIN();

    static int update_c_pc4(int samplerate) {

        /* bit 4 of latch C: (part of videoreg_w) hi? */
        if ((sound_latch_c & 0x10) != 0) {
            if (level_pc4 < VMAX) {
                counter_pc4 -= (int) ((VMAX - level_pc4) / pc4_charge_time);
                if (counter_pc4 <= 0) {
                    int n = (-counter_pc4 / samplerate) + 1;
                    counter_pc4 += n * samplerate;
                    if ((level_pc4 += n) > VMAX) {
                        level_pc4 = VMAX;
                    }
                }
            }
        } else {
            if (level_pc4 > PC4_MIN()) {
                counter_pc4 -= (int) ((level_pc4 - PC4_MIN()) / pc4_discharge_time);
                if (counter_pc4 <= 0) {
                    int n = (-counter_pc4 / samplerate) + 1;
                    counter_pc4 += n * samplerate;
                    if ((level_pc4 -= n) < PC4_MIN()) {
                        level_pc4 = PC4_MIN();
                    }
                }
            }
        }
        return level_pc4;
    }
    static int counter_pc5, level_pc5;

    static int update_c_pc5(int samplerate) {

        /* bit 5 of latch C: charge or discharge C52 */
        if ((sound_latch_c & 0x20) != 0) {
            if (level_pc5 < VMAX) {
                counter_pc5 -= (int) ((VMAX - level_pc5) / pc5_charge_time);
                if (counter_pc5 <= 0) {
                    int n = (-counter_pc5 / samplerate) + 1;
                    counter_pc5 += n * samplerate;
                    if ((level_pc5 += n) > VMAX) {
                        level_pc5 = VMAX;
                    }
                }
            }
        } else {
            if (level_pc5 > VMIN) {
                counter_pc5 -= (int) ((level_pc5 - VMIN) / pc5_discharge_time);
                if (counter_pc5 <= 0) {
                    int n = (-counter_pc5 / samplerate) + 1;
                    counter_pc5 += samplerate;
                    if ((level_pc5 -= n) < VMIN) {
                        level_pc5 = VMIN;
                    }
                }
            }
        }
        return level_pc5;
    }
    static int counter_pa5, level_pa5;

    static int update_c_pa5(int samplerate) {

        /* bit 5 of latch A: charge or discharge C63 */
        if ((sound_latch_a & 0x20) != 0) {
            if (level_pa5 < VMAX) {
                counter_pa5 -= (int) ((VMAX - level_pa5) / pa5_charge_time);
                if (counter_pa5 <= 0) {
                    int n = (-counter_pa5 / samplerate) + 1;
                    counter_pa5 += n * samplerate;
                    if ((level_pa5 += n) > VMAX) {
                        level_pa5 = VMAX;
                    }
                }
            }
        } else {
            if (level_pa5 > VMIN) {
                counter_pa5 -= (int) ((level_pa5 - VMIN) / pa5_discharge_time);
                if (counter_pa5 <= 0) {
                    int n = (-counter_pa5 / samplerate) + 1;
                    counter_pa5 += samplerate;
                    if ((level_pa5 -= n) < VMIN) {
                        level_pa5 = VMIN;
                    }
                }
            }
        }
        return level_pa5;
    }
    static int counter_tone4, output_tone4;

    static int tone4(int samplerate) {
        int level_ = update_c_pc4(samplerate);
        int vpc5 = update_c_pc5(samplerate);
        int vpa5 = update_c_pa5(samplerate);
        int sum;

        /* Two resistors divide the output_tone4 voltage of the op-amp between
		 * polybit = 0: 0V and level: x * opamp_resistor / (opamp_resistor + polybit_resistor)
		 * polybit = 1: level and 5V: x * polybit_resistor / (opamp_resistor + polybit_resistor)
         */
        if (polybit != 0) {
            level_ = level_ + (VMAX - level_) * opamp_resistor / (opamp_resistor + polybit_resistor);
        } else {
            level_ = level_ * polybit_resistor / (opamp_resistor + polybit_resistor);
        }

        counter_tone4 -= tone4_max_freq * level_ / 32768;
        if (counter_tone4 <= 0) {
            int n = (-counter_tone4 / samplerate) + 1;
            counter_tone4 += n * samplerate;
            output_tone4 = (output_tone4 + n) & 1;
        }

        /* mix the two signals */
        sum = vpc5 * pa5_resistor / (pa5_resistor + pc5_resistor)
                + vpa5 * pc5_resistor / (pa5_resistor + pc5_resistor);

        return (output_tone4) != 0 ? sum : -sum;
    }

    /**
     * ***************************************************************************
     * Noise comes from a shift register (4006) hooked up just like in Phoenix.
     * Difference: the clock frequecy is toggled between two values only by bit
     * 4 of latch A. The output of the first shift register can be zapped(?) by
     * some control line (IC87 in Pop Flamer: not yet implemented)
     * ***************************************************************************
     */
    static int counter_pa6, level_pa6;

    static int update_c_pa6(int samplerate) {

        /* bit 6 of latch A: charge or discharge C63 */
        if ((sound_latch_a & 0x40) != 0) {
            if (level_pa6 < VMAX) {
                counter_pa6 -= (int) ((VMAX - level_pa6) / pa6_charge_time);
                if (counter_pa6 <= 0) {
                    int n = (-counter_pa6 / samplerate) + 1;
                    counter_pa6 += n * samplerate;
                    if ((level_pa6 += n) > VMAX) {
                        level_pa6 = VMAX;
                    }
                }
            }
        } else {
            /* only discharge of poly bit is active */
            if (polybit != 0 && level_pa6 > VMIN) {
                /* discharge 10uF through 10k . 0.1s */
                counter_pa6 -= (int) ((level_pa6 - VMIN) / 0.1);
                if (counter_pa6 <= 0) {
                    int n = (-counter_pa6 / samplerate) + 1;
                    counter_pa6 += n * samplerate;
                    if ((level_pa6 -= n) < VMIN) {
                        level_pa6 = VMIN;
                    }
                }
            }
        }
        return level_pa6;
    }

    static int counter_n, polyoffs_n;

    static int noise(int samplerate) {
        int c_pa6_level = update_c_pa6(samplerate);
        int sum = 0;

        /*
		 * bit 4 of latch A: noise counter_n rate modulation?
		 * CV2 input of lower 556 is connected via 2k resistor
         */
        if ((sound_latch_a & 0x10) != 0) {
            counter_n -= noise_freq * 2 / 3;
            /* ????? */
        } else {
            counter_n -= noise_freq * 1 / 3;
            /* ????? */
        }

        if (counter_n <= 0) {
            int n = (-counter_n / samplerate) + 1;
            counter_n += n * samplerate;
            polyoffs_n = (polyoffs_n + n) & 0x3ffff;
            polybit = (poly18[polyoffs_n >> 5] >> (polyoffs_n & 31)) & 1;
        }

        /* The polynome output bit is used to gate bits 6 + 7 of
		 * sound latch A through the upper half of a 4066 chip.
		 * Bit 6 is sweeping a capacitor between 0V and 4.7V
		 * while bit 7 is connected directly to the 4066.
		 * Both outputs are then filtered, bit 7 even twice,
		 * but it's beyond me what the filters there are doing...
         */
        if (polybit != 0) {
            sum += c_pa6_level;
            /* bit 7 is connected directly */
            if ((sound_latch_a & 0x80) != 0) {
                sum += VMAX;
            }
        } else {
            sum -= c_pa6_level;
            /* bit 7 is connected directly */
            if ((sound_latch_a & 0x80) != 0) {
                sum -= VMAX;
            }
        }

        return sum / 2;
    }

    public static StreamInitPtr pleiads_sound_update = new StreamInitPtr() {
        public void handler(int param, ShortPtr buffer, int length) {
            int rate = Machine.sample_rate;

            while (length-- > 0) {
                int sum = tone1(rate) / 2 + tone23(rate) / 2 + tone4(rate) + noise(rate);
                buffer.writeinc(sum < 32768 ? sum > -32768 ? (short) sum : -32768 : 32767);
            }
        }
    };

    public static WriteHandlerPtr pleiads_sound_control_a_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (data == sound_latch_a) {
                return;
            }

            if (errorlog != null) {
                fprintf(errorlog, "pleiads_sound_control_b_w $%02x\n", data);
            }

            stream_update(channel, 0);
            sound_latch_a = data;
        }
    };

    public static WriteHandlerPtr pleiads_sound_control_b_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*
		 * pitch selects one of 4 possible clock inputs
		 * (actually 3, because IC2 and IC3 are tied together)
		 * write note value to TMS3615; voice b1 & b2
             */
            int note = data & 15;
            int pitch = (data >> 6) & 3;

            if (data == sound_latch_b) {
                return;
            }

            if (errorlog != null) {
                fprintf(errorlog, "pleiads_sound_control_b_w $%02x\n", data);
            }

            if (pitch == 3) {
                pitch = 2;
                /* 2 and 3 are the same */
            }

            tms36xx_note_w(0, pitch, note);

            stream_update(channel, 0);
            sound_latch_b = data;
        }
    };

    /* two bits (4 + 5) from the videoreg_w latch go here */
    public static WriteHandlerPtr pleiads_sound_control_c_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (data == sound_latch_c) {
                return;
            }

            if (errorlog != null) {
                fprintf(errorlog, "pleiads_sound_control_c_w $%02x\n", data);
            }
            stream_update(channel, 0);
            sound_latch_c = data;
        }
    };

    static int common_sh_start(MachineSound msound, String name) {
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

        channel = stream_init(name, 40, Machine.sample_rate, 0, pleiads_sound_update);
        if (channel == -1) {
            return 1;
        }

        return 0;
    }

    public static ShStartHandlerPtr pleiads_sh_start = new ShStartHandlerPtr() {
        public int handler(MachineSound msound) {
            /* The real values are _unknown_!
		 * I took the ones from Naughty Boy / Pop Flamer
             */

 /* charge 10u?? (C??) through 330K?? (R??) . 3.3s */
            pa5_charge_time = 3.3;

            /* discharge 10u?? (C??) through 220k?? (R??) . 2.2s */
            pa5_discharge_time = 2.2;

            /* charge 2.2uF?? through 330?? . 0.000726s */
            pa6_charge_time = 0.000726;

            /* discharge 2.2uF?? through 10k?? . 0.22s */
            pa6_discharge_time = 0.022;

            /* 10k and 10uF */
            pb4_charge_time = 0.1;
            pb4_discharge_time = 0.1;

            /* charge C49 (22u?) via R47 (2k?) and R48 (1k)
		 * time constant (1000+2000) * 22e-6 = 0.066s */
            pc4_charge_time = 0.066;

            /* discharge C49 (22u?) via R48 (1k) and diode D1
		 * time constant 1000 * 22e-6 = 0.022s */
            pc4_discharge_time = 0.022;

            /* charge 10u?? through 330 . 0.0033s */
            pc5_charge_time = 0.0033;

            /* discharge 10u?? through ??k (R??) . 0.1s */
            pc5_discharge_time = 0.1;

            /* both in K */
            pa5_resistor = 33;
            pc5_resistor = 47;

            /* upper 556 upper half: Ra=10k??, Rb=200k??, C=0.01uF?? . 351Hz */
            tone2_max_freq = 351;

            /* upper 556 lower half: Ra=47k??, Rb=100k??, C=0.01uF?? . 582Hz */
            tone3_max_freq = 582;

            /* lower 556 upper half: Ra=33k??, Rb=100k??, C=0.0047uF??
		   freq = 1.44 / ((33000+2*100000) * 0.0047e-6) = approx. 1315 Hz */
            tone4_max_freq = 1315;

            /* how to divide the V/C voltage for tone #4 */
            polybit_resistor = 47;
            opamp_resistor = 20;

            /* lower 556 lower half: Ra=100k??, Rb=1k??, C=0.01uF??
		  freq = 1.44 / ((100000+2*1000) * 0.01e-6) = approx. 1412 Hz */
            noise_freq = 1412;
            /* higher noise rate than popflame/naughtyb??? */

            return common_sh_start(msound, "Custom (Pleiads)");
        }
    };

    public static ShStartHandlerPtr naughtyb_sh_start = new ShStartHandlerPtr() {
        public int handler(MachineSound msound) {
            /* charge 10u??? through 330K (R??) . 3.3s */
            pa5_charge_time = 3.3;

            /* discharge 10u through 220k (R??) . 2.1s */
            pa5_discharge_time = 2.2;

            /* charge 2.2uF through 330 . 0.000726s */
            pa6_charge_time = 0.000726;

            /* discharge 2.2uF through 10K . 0.022s */
            pa6_discharge_time = 0.022;

            /* 10k and 10uF */
            pb4_charge_time = 0.1;
            pb4_discharge_time = 0.1;

            /* charge 10uF? (C??) via 3k?? (R??) and 2k?? (R28?)
		 * time constant (3000+2000) * 10e-6 = 0.05s */
            pc4_charge_time = 0.05 * 10;

            /* discharge 10uF? (C??) via 2k?? R28??  and diode D?
		 * time constant 2000 * 10e-6 = 0.02s */
            pc4_discharge_time = 0.02 * 10;

            /* charge 10u through 330 . 0.0033s */
            pc5_charge_time = 0.0033;

            /* discharge 10u through ??k (R??) . 0.1s */
            pc5_discharge_time = 0.1;

            /* both in K */
            pa5_resistor = 100;
            pc5_resistor = 78;

            /* upper 556 upper half: 10k, 200k, 0.01uF . 351Hz */
            tone2_max_freq = 351;

            /* upper 556 lower half: 47k, 200k, 0.01uF . 322Hz */
            tone3_max_freq = 322;

            /* lower 556 upper half: Ra=33k, Rb=100k, C=0.0047uF
		   freq = 1.44 / ((33000+2*100000) * 0.0047e-6) = approx. 1315 Hz */
            tone4_max_freq = 1315;

            /* how to divide the V/C voltage for tone #4 */
            polybit_resistor = 47;
            opamp_resistor = 20;

            /* lower 556 lower half: Ra=200k, Rb=1k, C=0.01uF
		  freq = 1.44 / ((200000+2*1000) * 0.01e-6) = approx. 713 Hz */
            noise_freq = 713;

            return common_sh_start(msound, "Custom (Naughty Boy)");
        }
    };

    public static ShStartHandlerPtr popflame_sh_start = new ShStartHandlerPtr() {
        public int handler(MachineSound msound) {
            /* charge 10u (C63 in Pop Flamer) through 330K . 3.3s */
            pa5_charge_time = 3.3;

            /* discharge 10u (C63 in Pop Flamer) through 220k . 2.2s */
            pa5_discharge_time = 2.2;

            /* charge 2.2uF through 330 . 0.000726s */
            pa6_charge_time = 0.000726;

            /* discharge 2.2uF through 10K . 0.022s */
            pa6_discharge_time = 0.022;

            /* 2k and 10uF */
            pb4_charge_time = 0.02;
            pb4_discharge_time = 0.02;

            /* charge 2.2uF (C49?) via R47 (100) and R48 (1k)
		 * time constant (100+1000) * 2.2e-6 = 0.00242 */
            pc4_charge_time = 0.000242;

            /* discharge 2.2uF (C49?) via R48 (1k) and diode D1
		 * time constant 1000 * 22e-6 = 0.0022s */
            pc4_discharge_time = 0.00022;

            /* charge 22u (C52 in Pop Flamer) through 10k . 0.22s */
            pc5_charge_time = 0.22;

            /* discharge 22u (C52 in Pop Flamer) through ??k (R??) . 0.1s */
            pc5_discharge_time = 0.1;

            /* both in K */
            pa5_resistor = 33;
            pc5_resistor = 47;

            /* upper 556 upper half: Ra=10k, Rb=100k, C=0.01uF . 1309Hz */
            tone2_max_freq = 1309;

            /* upper 556 lower half: Ra=10k??, Rb=120k??, C=0.01uF . 1108Hz */
            tone3_max_freq = 1108;

            /* lower 556 upper half: Ra=33k, Rb=100k, C=0.0047uF
		   freq = 1.44 / ((33000+2*100000) * 0.0047e-6) = approx. 1315 Hz */
            tone4_max_freq = 1315;

            /* how to divide the V/C voltage for tone #4 */
            polybit_resistor = 20;
            opamp_resistor = 20;

            /* lower 556 lower half: Ra=200k, Rb=1k, C=0.01uF
		  freq = 1.44 / ((200000+2*1000) * 0.01e-6) = approx. 713 Hz */
            noise_freq = 713;

            return common_sh_start(msound, "Custom (Pop Flamer)");
        }
    };

    public static ShStopHandlerPtr pleiads_sh_stop = new ShStopHandlerPtr() {
        public void handler() {
            if (poly18 != null) {
                poly18 = null;
            }
        }
    };

    public static ShUpdateHandlerPtr pleiads_sh_update = new ShUpdateHandlerPtr() {
        public void handler() {
            stream_update(channel, 0);
        }
    };

}
