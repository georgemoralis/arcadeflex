/*
 * ported to v0.36
 * 
 */
/**
 * Changelog
 * =========
 * 15/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.sndhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.sndintrfH.*;
//sound imports
import static arcadeflex.v036.sound.streams.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.sound.mixer.*;

public class bzone {

    /* Statics */
    static ShortPtr discharge = null;

    static int EXP(int charge, int n) {
        return (charge != 0 ? 0x7fff - discharge.read(0x7fff - n) : discharge.read(n));
    }

    static int channel;
    static int latch;
    static int poly_counter;
    static int poly_shift;

    static int explosion_clock;
    static int explosion_out;
    static int explosion_amp;
    static int explosion_amp_counter;

    static int shell_clock;
    static int shell_out;
    static int shell_amp;
    static int shell_amp_counter;

    static int motor_counter;
    static int motor_counter_a;
    static int motor_counter_b;
    static int motor_rate;
    static int motor_rate_new;
    static int motor_rate_counter;
    static int motor_amp;
    static int motor_amp_new;
    static int motor_amp_step;
    static int motor_amp_counter;

    public static WriteHandlerPtr bzone_sounds_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (data == latch) {
                return;
            }

            stream_update(channel, 0);
            latch = data;

            mixer_sound_enable_global_w(latch & 0x20);
        }
    };
    static int last_val = 0;
    static double r0 = 1.0 / 1e12, r1 = 1.0 / 1e12;
    public static StreamInitPtr bzone_sound_update = new StreamInitPtr() {
        public void handler(int param, ShortPtr buffer, int length) {
            while (length-- != 0) {

                int sum = 0;

                /* polynome shifter H5 and H4 (LS164) clocked with 6kHz */
                poly_counter -= 6000;
                while (poly_counter <= 0) {
                    int clock;

                    poly_counter += Machine.sample_rate;
                    if (((poly_shift & 0x0008) == 0) == ((poly_shift & 0x4000) == 0)) {
                        poly_shift = (poly_shift << 1) | 1;
                    } else {
                        poly_shift <<= 1;
                    }

                    /* NAND gate J4 */
                    clock = ((poly_shift & 0x7000) == 0x7000) ? 0 : 1;

                    /* raising edge on pin 3 of J5 (LS74)? */
                    if (clock != 0 && explosion_clock == 0) {
                        explosion_out ^= 1;
                    }

                    /* save explo clock level */
                    explosion_clock = clock;

                    /* input 11 of J5 (LS74) */
                    clock = (poly_shift >> 15) & 1;

                    /* raising edge on pin 11 of J5 (LS74)? */
                    if (clock != 0 && shell_clock == 0) {
                        shell_out ^= 1;
                    }

                    /* save shell clock level */
                    shell_clock = clock;
                }

                /* explosion enable: charge C14 */
                if ((latch & 0x01) != 0) {
                    explosion_amp = 32767;
                }

                /* explosion output? */
                if (explosion_out != 0) {
                    if (explosion_amp > 0) {
                        /*
					 * discharge C14 through R17 + R16
					 * time constant is 10e-6 * 23000 = 0.23 seconds
					 * (samples were decaying much slower: 1/4th rate? )
                         */
                        explosion_amp_counter -= (int) (32767 / (0.23 * 4));
                        if (explosion_amp_counter < 0) {
                            int n = (-explosion_amp_counter / Machine.sample_rate) + 1;
                            explosion_amp_counter += n * Machine.sample_rate;
                            if ((explosion_amp -= n) < 0) {
                                explosion_amp = 0;
                            }
                        }
                    }
                    /*
				 * I don't know the amplification of the op-amp
				 * and feedback, so the loud/soft values are arbitrary
                     */
                    if ((latch & 0x02) != 0) /* explosion loud ? */ {
                        sum += EXP(0, explosion_amp) / 3;
                    } else {
                        sum += EXP(0, explosion_amp) / 4;
                    }
                }

                /* shell enable: charge C9 */
                if ((latch & 0x04) != 0) {
                    shell_amp = 32767;
                }

                /* shell output? */
                if (shell_out != 0) {
                    if (shell_amp > 0) {
                        /*
					 * discharge C9 through R14 + R15
					 * time constant is 4.7e-6 * 23000 = 0.1081 seconds
					 * (samples were decaying much slower: 1/4th rate? )
                         */
                        shell_amp_counter -= (int) (32767 / (0.1081 * 4));
                        if (shell_amp_counter < 0) {
                            int n = (-shell_amp_counter / Machine.sample_rate) + 1;
                            shell_amp_counter += n * Machine.sample_rate;
                            if ((shell_amp -= n) < 0) {
                                shell_amp = 0;
                            }
                        }
                    }
                    /*
				 * I don't know the amplification of the op-amp
				 * and feedback, so the loud/soft values are arbitrary
                     */
                    if ((latch & 0x08) != 0) /* shell loud ? */ {
                        sum += EXP(0, shell_amp) / 3;
                    } else {
                        sum += EXP(0, shell_amp) / 4;
                    }
                }

                if ((latch & 0x80) != 0) {

                    /* NE5555 timer
				 * C = 0.018u, Ra = 100k, Rb = 125k
				 * charge time = 0.693 * (Ra + Rb) * C = 3870us
				 * discharge time = 0.693 * Rb * C = 1559.25us
				 * freq approx. 184 Hz
				 * I have no idea what frequencies are coming from the NE555
				 * with "MOTOR REV EN" being high or low. I took 240Hz as
				 * higher rate and sweep up or down to the new rate in 0.25s
                     */
                    motor_rate_new = (latch & 0x10) != 0 ? 240 : 184;
                    if (motor_rate != motor_rate_new) {
                        /* sweep rate to new rate */
                        motor_rate_counter -= (int) ((240 - 184) / 0.25);
                        while (motor_rate_counter <= 0) {
                            motor_rate_counter += Machine.sample_rate;
                            motor_rate += (motor_rate < motor_rate_new) ? +1 : -1;
                        }
                    }
                    motor_counter -= motor_rate;
                    while (motor_counter <= 0) {
                        motor_counter += Machine.sample_rate;

                        r0 = 1.0 / 1e12;
                        r1 = 1.0 / 1e12;

                        if (++motor_counter_a == 16) {
                            motor_counter_a = 6;
                        }
                        if (++motor_counter_b == 16) {
                            motor_counter_b = 4;
                        }

                        if ((motor_counter_a & 8) != 0) /* bit 3 */ {
                            r1 += 1.0 / 33000;
                        } else {
                            r0 += 1.0 / 33000;
                        }
                        if (motor_counter_a == 15) /* ripple carry */ {
                            r1 += 1.0 / 33000;
                        } else {
                            r0 += 1.0 / 33000;
                        }

                        if ((motor_counter_b & 8) != 0) /* bit 3 */ {
                            r1 += 1.0 / 33000;
                        } else {
                            r0 += 1.0 / 33000;
                        }
                        if (motor_counter_b == 15) /* ripple carry */ {
                            r1 += 1.0 / 33000;
                        } else {
                            r0 += 1.0 / 33000;
                        }

                        /* new voltage at C29 */
                        r0 = 1.0 / r0;
                        r1 = 1.0 / r1;
                        motor_amp_new = (int) (32767 * r0 / (r0 + r1));

                        /* charge/discharge C29 (0.47uF) */
                        if (motor_amp_new > motor_amp) {
                            motor_amp_step = (int) ((motor_amp_new - motor_amp) / (r1 * 0.47e-6));
                        } else {
                            motor_amp_step = (int) ((motor_amp - motor_amp_new) / (r0 * 0.47e-6));
                        }
                    }
                    if (motor_amp != motor_amp_new) {
                        motor_amp_counter -= motor_amp_step;
                        if (motor_amp_counter < 0) {
                            int n = (-motor_amp_counter / Machine.sample_rate) + 1;
                            motor_amp_counter += n * Machine.sample_rate;
                            if (motor_amp > motor_amp_new) {
                                motor_amp -= n;
                                if (motor_amp < motor_amp_new) {
                                    motor_amp = motor_amp_new;
                                }
                            } else {
                                motor_amp += n;
                                if (motor_amp > motor_amp_new) {
                                    motor_amp = motor_amp_new;
                                }
                            }
                        }
                    }
                    sum += EXP((motor_amp < motor_amp_new) ? 1 : 0, motor_amp) / 3;
                }

                buffer.writeinc((short) ((sum + last_val) / 2));

                /* crude 75% low pass filter */
                last_val = (sum + last_val * 3) / 4;
            }
        }
    };

    public static ShStartHandlerPtr bzone_sh_start = new ShStartHandlerPtr() {
        public int handler(MachineSound msound) {
            int i;

            discharge = new ShortPtr(32768 * 2);
            if (discharge == null) {
                return 1;
            }

            for (i = 0; i < 0x8000; i++) {
                discharge.write(0x7fff - i, (short) (0x7fff / Math.exp(1.0 * i / 4096)));
            }

            channel = stream_init("Custom", 50, Machine.sample_rate, 0, bzone_sound_update);
            if (channel == -1) {
                return 1;
            }

            return 0;
        }
    };

    public static ShStopHandlerPtr bzone_sh_stop = new ShStopHandlerPtr() {
        public void handler() {
            if (discharge != null) {
                discharge = null;
            }
        }
    };

    public static ShUpdateHandlerPtr bzone_sh_update = new ShUpdateHandlerPtr() {
        public void handler() {
            stream_update(channel, 0);
        }
    };

}
