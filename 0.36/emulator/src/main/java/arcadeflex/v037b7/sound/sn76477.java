/*
 * ported to v0.37b7
 *
 */
/**
 * Changelog
 * =========
 * 02/02/2023 - shadow - This file should be complete for 0.37b7 version
 */
package arcadeflex.v037b7.sound;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;
//sound imports
import static arcadeflex.v036.sound.streams.*;
import static arcadeflex.v037b7.sound.sn76477H.*;
//sound imports;
import static common.libc.cstring.*;
import static common.libc.expressions.*;
//to be organized
import gr.codebb.arcadeflex.common.PtrLib.ShortPtr;
import static gr.codebb.arcadeflex.v036.platform.libc_old.sprintf;

public class sn76477 extends snd_interface {

    public static int VMIN = 0x0000;
    public static int VMAX = 0x7fff;

    public sn76477() {
        sound_num = SOUND_SN76477;
        name = "SN76477";
    }

    public static class _SN76477 {

        int channel;
        /* returned by stream_init() */

        int samplerate;
        /* from Machine.sample_rate */
        int vol;
        /* current volume (attack/decay) */
        int vol_count;
        /* volume adjustment counter */
        int vol_rate;
        /* volume adjustment rate - dervied from attack/decay */
        int vol_step;
        /* volume adjustment step */

        double slf_count;
        /* SLF emulation */
        double slf_freq;
        /* frequency - derived */
        double slf_level;
        /* triangular wave level */
        int slf_dir;
        /* triangular wave direction */
        int slf_out;
        /* rectangular output signal state */

        double vco_count;
        /* VCO emulation */
        double vco_freq;
        /* frequency - derived */
        double vco_step;
        /* modulated frequency - derived */
        int vco_out;
        /* rectangular output signal state */

        int noise_count;
        /* NOISE emulation */
        int noise_clock;
        /* external clock signal */
        int noise_freq;
        /* filter frequency - derived */
        int noise_poly;
        /* polynome */
        int noise_out;
        /* rectangular output signal state */

        Object envelope_timer;
        /* ENVELOPE timer */
        int envelope_state;
        /* attack / decay toggle */

        double attack_time;
        /* ATTACK time (time until vol reaches 100%) */
        double decay_time;
        /* DECAY time (time until vol reaches 0%) */
        double oneshot_time;
        /* ONE-SHOT time */
        Object oneshot_timer;
        /* ONE-SHOT timer */

        int envelope;
        /* pin	1, pin 28 */
        double noise_res;
        /* pin	4 */
        double filter_res;
        /* pin	5 */
        double filter_cap;
        /* pin	6 */
        double decay_res;
        /* pin	7 */
        double attack_decay_cap;/* pin	8 */
        int enable;
        /* pin	9 */
        double attack_res;
        /* pin 10 */
        double amplitude_res;
        /* pin 11 */
        double feedback_res;
        /* pin 12 */
        double vco_voltage;
        /* pin 16 */
        double vco_cap;
        /* pin 17 */
        double vco_res;
        /* pin 18 */
        double pitch_voltage;
        /* pin 19 */
        double slf_res;
        /* pin 20 */
        double slf_cap;
        /* pin 21 */
        int vco_select;
        /* pin 22 */
        double oneshot_cap;
        /* pin 23 */
        double oneshot_res;
        /* pin 24 */
        int mixer;
        /* pin 25,26,27 */

        short[] vol_lookup = new short[VMAX + 1 - VMIN];
        /* volume lookup table */
    };

    static SN76477interface intf;
    static _SN76477[] sn76477 = new _SN76477[MAX_SN76477];

    static void attack_decay(int param) {
        _SN76477 sn = sn76477[param];
        sn.envelope_state ^= 1;
        if (sn.envelope_state != 0) {
            /* start ATTACK */
            sn.vol_rate = (sn.attack_time > 0) ? (int) (VMAX / sn.attack_time) : VMAX;
            sn.vol_step = +1;
            //LOG(2,("SN76477 #%d: ATTACK rate %d/%d = %d/sec\n", param, sn.vol_rate, sn.samplerate, sn.vol_rate/sn.samplerate));
        } else {
            /* start DECAY */
            sn.vol = VMAX;
            /* just in case... */
            sn.vol_rate = (sn.decay_time > 0) ? (int) (VMAX / sn.decay_time) : VMAX;
            sn.vol_step = -1;
            //LOG(2,("SN76477 #%d: DECAY rate %d/%d = %d/sec\n", param, sn.vol_rate, sn.samplerate, sn.vol_rate/sn.samplerate));
        }
    }
    public static TimerCallbackHandlerPtr vco_envelope_cb = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            attack_decay(param);
        }
    };
    public static TimerCallbackHandlerPtr oneshot_envelope_cb = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            _SN76477 sn = sn76477[param];
            sn.oneshot_timer = null;
            attack_decay(param);
        }
    };

    /**
     * ***************************************************************************
     * set MIXER select inputs
     * ***************************************************************************
     */
    public static void SN76477_mixer_w(int chip, int data) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM_AND_RANGE(7,SN76477_mixer_w);
        if (data == sn.mixer) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.mixer = data;
        //LOG(1,("SN76477 #%d: MIXER mode %d [%s]\n", chip, sn.mixer, mixer_mode[sn.mixer]));
    }

    public static void SN76477_mixer_a_w(int chip, int data) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM_AND_RANGE(1,SN76477_mixer_a_w);
        data = data != 0 ? 1 : 0;
        if (data == (sn.mixer & 1)) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.mixer = (sn.mixer & ~1) | data;
        //LOG(1,("SN76477 #%d: MIXER mode %d [%s]\n", chip, sn.mixer, mixer_mode[sn.mixer]));
    }

    public static void SN76477_mixer_b_w(int chip, int data) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM_AND_RANGE(1,SN76477_mixer_b_w);
        data = data != 0 ? 2 : 0;
        if (data == (sn.mixer & 2)) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.mixer = (sn.mixer & ~2) | data;
        //LOG(1,("SN76477 #%d: MIXER mode %d [%s]\n", chip, sn.mixer, mixer_mode[sn.mixer]));
    }

    public static void SN76477_mixer_c_w(int chip, int data) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM_AND_RANGE(1,SN76477_mixer_c_w);
        data = data != 0 ? 4 : 0;
        if (data == (sn.mixer & 4)) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.mixer = (sn.mixer & ~4) | data;
        //LOG(1,("SN76477 #%d: MIXER mode %d [%s]\n", chip, sn.mixer, mixer_mode[sn.mixer]));
    }

    /**
     * ***************************************************************************
     * set ENVELOPE select inputs
     * ***************************************************************************
     */
    public static void SN76477_envelope_w(int chip, int data) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM_AND_RANGE(3,SN76477_envelope_w);
        if (data == sn.envelope) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.envelope = data;
        //LOG(1,("SN76477 #%d: ENVELOPE mode %d [%s]\n", chip, sn.envelope, envelope_mode[sn.envelope]));
    }

    public static void SN76477_envelope_1_w(int chip, int data) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM_AND_RANGE(1,SN76477_envelope_1_w);
        if (data == (sn.envelope & 1)) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.envelope = (sn.envelope & ~1) | data;
        //LOG(1,("SN76477 #%d: ENVELOPE mode %d [%s]\n", chip, sn.envelope, envelope_mode[sn.envelope]));
    }

    public static void SN76477_envelope_2_w(int chip, int data) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM_AND_RANGE(1,SN76477_envelope_2_w);
        data <<= 1;

        if (data == (sn.envelope & 2)) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.envelope = (sn.envelope & ~2) | data;
        //LOG(1,("SN76477 #%d: ENVELOPE mode %d [%s]\n", chip, sn.envelope, envelope_mode[sn.envelope]));
    }

    /**
     * ***************************************************************************
     * set VCO external/SLF input
     * ***************************************************************************
     */
    public static void SN76477_vco_w(int chip, int data) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM_AND_RANGE(1,SN76477_vco_w);
        if (data == sn.vco_select) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.vco_select = data;
        //LOG(1,("SN76477 #%d: VCO select %d [%s]\n", chip, sn.vco_select, sn.vco_select ? "Internal (SLF)" : "External (Pin 16)"));
    }

    /**
     * ***************************************************************************
     * set VCO enable input
     * ***************************************************************************
     */
    public static void SN76477_enable_w(int chip, int data) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM_AND_RANGE(1,SN76477_enable_w);
        if (data == sn.enable) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.enable = data;
        sn.envelope_state = data;
        if (sn.envelope_timer != null) {
            timer_remove(sn.envelope_timer);
        }
        sn.envelope_timer = null;
        if (sn.oneshot_timer != null) {
            timer_remove(sn.oneshot_timer);
        }
        sn.oneshot_timer = null;
        if (sn.enable == 0) {
            switch (sn.envelope) {
                case 0:
                    /* VCO */
                    if (sn.vco_res > 0 && sn.vco_cap > 0) {
                        sn.envelope_timer = timer_pulse(TIME_IN_HZ(0.64 / (sn.vco_res * sn.vco_cap)), chip, vco_envelope_cb);
                    } else {
                        oneshot_envelope_cb.handler(chip);
                    }
                    break;
                case 1:
                    /* One-Shot */
                    oneshot_envelope_cb.handler(chip);
                    if (sn.oneshot_time > 0) {
                        sn.oneshot_timer = timer_set(sn.oneshot_time, chip, oneshot_envelope_cb);
                    }
                    break;
                case 2:
                    /* MIXER only */
                    sn.vol = VMAX;
                    break;
                default:
                    /* VCO with alternating polariy */
 /* huh? */
                    if (sn.vco_res > 0 && sn.vco_cap > 0) {
                        sn.envelope_timer = timer_pulse(TIME_IN_HZ(0.64 / (sn.vco_res * sn.vco_cap) / 2), chip, vco_envelope_cb);
                    } else {
                        oneshot_envelope_cb.handler(chip);
                    }
                    break;
            }
        } else {
            switch (sn.envelope) {
                case 0:
                    /* VCO */
                    if (sn.vco_res > 0 && sn.vco_cap > 0) {
                        sn.envelope_timer = timer_pulse(TIME_IN_HZ(0.64 / (sn.vco_res * sn.vco_cap)), chip, vco_envelope_cb);
                    } else {
                        oneshot_envelope_cb.handler(chip);
                    }
                    break;
                case 1:
                    /* One-Shot */
                    oneshot_envelope_cb.handler(chip);
                    break;
                case 2:
                    /* MIXER only */
                    sn.vol = VMIN;
                    break;
                default:
                    /* VCO with alternating polariy */
 /* huh? */
                    if (sn.vco_res > 0 && sn.vco_cap > 0) {
                        sn.envelope_timer = timer_pulse(TIME_IN_HZ(0.64 / (sn.vco_res * sn.vco_cap) / 2), chip, vco_envelope_cb);
                    } else {
                        oneshot_envelope_cb.handler(chip);
                    }
                    break;
            }
        }
        //LOG(1,("SN76477 #%d: ENABLE line %d [%s]\n", chip, sn.enable, sn.enable ? "Inhibited" : "Enabled" ));
    }

    /**
     * ***************************************************************************
     * set NOISE external signal (pin 3)
     * ***************************************************************************
     */
    public static void SN76477_noise_clock_w(int chip, int data) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM_AND_RANGE(1,SN76477_noise_clock_w);
        if (data == sn.noise_clock) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.noise_clock = data;
        /* on the rising edge shift the polynome */
        if (sn.noise_clock != 0) {
            sn.noise_poly = ((sn.noise_poly << 7) + (sn.noise_poly >> 10) + 0x18000) & 0x1ffff;
        }
    }

    /**
     * ***************************************************************************
     * set NOISE resistor (pin 4)
     * ***************************************************************************
     */
    public static void SN76477_set_noise_res(int chip, double res) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM;
        stream_update(sn.channel, 0);
        sn.noise_res = res;
    }

    /**
     * ***************************************************************************
     * set NOISE FILTER resistor (pin 5)
     * ***************************************************************************
     */
    public static void SN76477_set_filter_res(int chip, double res) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM;
        if (res == sn.filter_res) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.filter_res = res;
        if (sn.filter_res > 0 && sn.filter_cap > 0) {
            sn.noise_freq = (int) (1.28 / (sn.filter_res * sn.filter_cap));
            //LOG(1,("SN76477 #%d: NOISE FILTER freqency %d\n", chip, sn.noise_freq));
        } else {
            sn.noise_freq = sn.samplerate;
        }
    }

    /**
     * ***************************************************************************
     * set NOISE FILTER capacitor (pin 6)
     * ***************************************************************************
     */
    public static void SN76477_set_filter_cap(int chip, double cap) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM;
        if (cap == sn.filter_cap) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.filter_cap = cap;
        if (sn.filter_res > 0 && sn.filter_cap > 0) {
            sn.noise_freq = (int) (1.28 / (sn.filter_res * sn.filter_cap));
            //LOG(1,("SN76477 #%d: NOISE FILTER freqency %d\n", chip, sn.noise_freq));
        } else {
            sn.noise_freq = sn.samplerate;
        }
    }

    /**
     * ***************************************************************************
     * set DECAY resistor (pin 7)
     * ***************************************************************************
     */
    public static void SN76477_set_decay_res(int chip, double res) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM;
        if (res == sn.decay_res) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.decay_res = res;
        sn.decay_time = sn.decay_res * sn.attack_decay_cap;
        //LOG(1,("SN76477 #%d: DECAY time is %fs\n", chip, sn.decay_time));
    }

    /**
     * ***************************************************************************
     * set ATTACK/DECAY capacitor (pin 8)
     * ***************************************************************************
     */
    public static void SN76477_set_attack_decay_cap(int chip, double cap) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM;
        if (cap == sn.attack_decay_cap) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.attack_decay_cap = cap;
        sn.decay_time = sn.decay_res * sn.attack_decay_cap;
        sn.attack_time = sn.attack_res * sn.attack_decay_cap;
        //LOG(1,("SN76477 #%d: ATTACK time is %fs\n", chip, sn.attack_time));
        //LOG(1,("SN76477 #%d: DECAY time is %fs\n", chip, sn.decay_time));
    }

    /**
     * ***************************************************************************
     * set ATTACK resistor (pin 10)
     * ***************************************************************************
     */
    public static void SN76477_set_attack_res(int chip, double res) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM;
        if (res == sn.attack_res) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.attack_res = res;
        sn.attack_time = sn.attack_res * sn.attack_decay_cap;
        //LOG(1,("SN76477 #%d: ATTACK time is %fs\n", chip, sn.attack_time));
    }

    /**
     * ***************************************************************************
     * set AMP resistor (pin 11)
     * ***************************************************************************
     */
    public static void SN76477_set_amplitude_res(int chip, double res) {
        _SN76477 sn = sn76477[chip];
        int i;

        //CHECK_CHIP_NUM;
        if (res == sn.amplitude_res) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.amplitude_res = res;
        if (sn.amplitude_res > 0) {
            for (i = 0; i < VMAX + 1; i++) {
                int vol = (int) ((3.4 * sn.feedback_res / sn.amplitude_res) * 32767 * i / (VMAX + 1));
                if (vol > 32767) {
                    vol = 32767;
                }
                sn.vol_lookup[i] = (short) (vol * intf.mixing_level[chip] / 100);
            }
            //LOG(1,("SN76477 #%d: volume range from -%d to +%d (clip at %d%%)\n", chip, sn.vol_lookup[VMAX-VMIN], sn.vol_lookup[VMAX-VMIN], clip * 100 / 256));
        } else {
            memset(sn.vol_lookup, 0, sizeof(sn.vol_lookup));
        }
    }

    /**
     * ***************************************************************************
     * set FEEDBACK resistor (pin 12)
     * ***************************************************************************
     */
    public static void SN76477_set_feedback_res(int chip, double res) {
        _SN76477 sn = sn76477[chip];
        int i;

        //CHECK_CHIP_NUM;
        if (res == sn.feedback_res) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.feedback_res = res;
        if (sn.amplitude_res > 0) {
            for (i = 0; i < VMAX + 1; i++) {
                int vol = (int) ((3.4 * sn.feedback_res / sn.amplitude_res) * 32767 * i / (VMAX + 1));
                if (vol > 32767) {
                    vol = 32767;
                }
                sn.vol_lookup[i] = (short) (vol * intf.mixing_level[chip] / 100);
            }
            //LOG(1,("SN76477 #%d: volume range from -%d to +%d (clip at %d%%)\n", chip, sn.vol_lookup[VMAX-VMIN], sn.vol_lookup[VMAX-VMIN], clip * 100 / 256));
        } else {
            memset(sn.vol_lookup, 0, sizeof(sn.vol_lookup));
        }
    }

    /**
     * ***************************************************************************
     * set PITCH voltage (pin 19) TODO: fill with live...
     * ***************************************************************************
     */
    public static void SN76477_set_pitch_voltage(int chip, double voltage) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM;
        if (voltage == sn.pitch_voltage) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.pitch_voltage = voltage;
        //LOG(1,("SN76477 #%d: VCO pitch voltage %f (%d%% duty cycle)\n", chip, sn.pitch_voltage, 0));
    }

    /**
     * ***************************************************************************
     * set VCO resistor (pin 18)
     * ***************************************************************************
     */
    public static void SN76477_set_vco_res(int chip, double res) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM;
        if (res == sn.vco_res) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.vco_res = res;
        if (sn.vco_res > 0 && sn.vco_cap > 0) {
            sn.vco_freq = 0.64 / (sn.vco_res * sn.vco_cap);
            //LOG(1,("SN76477 #%d: VCO freqency %f\n", chip, sn.vco_freq));
        } else {
            sn.vco_freq = 0;
        }
    }

    /**
     * ***************************************************************************
     * set VCO capacitor (pin 17)
     * ***************************************************************************
     */
    public static void SN76477_set_vco_cap(int chip, double cap) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM;
        if (cap == sn.vco_cap) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.vco_cap = cap;
        if (sn.vco_res > 0 && sn.vco_cap > 0) {
            sn.vco_freq = 0.64 / (sn.vco_res * sn.vco_cap);
            //LOG(1,("SN76477 #%d: VCO freqency %f\n", chip, sn.vco_freq));
        } else {
            sn.vco_freq = 0;
        }
    }

    /**
     * ***************************************************************************
     * set VCO voltage (pin 16)
     * ***************************************************************************
     */
    public static void SN76477_set_vco_voltage(int chip, double voltage) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM;
        if (voltage == sn.vco_voltage) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.vco_voltage = voltage;
        /*LOG(1,("SN76477 #%d: VCO ext. voltage %f (%f * %f = %f Hz)\n", chip,
			sn.vco_voltage,
			sn.vco_freq,
			10.0 * (5.0 - sn.vco_voltage) / 5.0,
			sn.vco_freq * 10.0 * (5.0 - sn.vco_voltage) / 5.0));*/
    }

    /**
     * ***************************************************************************
     * set SLF resistor (pin 20)
     * ***************************************************************************
     */
    public static void SN76477_set_slf_res(int chip, double res) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM;
        if (res == sn.slf_res) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.slf_res = res;
        if (sn.slf_res > 0 && sn.slf_cap > 0) {
            sn.slf_freq = 0.64 / (sn.slf_res * sn.slf_cap);
            //LOG(1,("SN76477 #%d: SLF freqency %f\n", chip, sn.slf_freq));
        } else {
            sn.slf_freq = 0;
        }
    }

    /**
     * ***************************************************************************
     * set SLF capacitor (pin 21)
     * ***************************************************************************
     */
    public static void SN76477_set_slf_cap(int chip, double cap) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM;
        if (cap == sn.slf_cap) {
            return;
        }
        stream_update(sn.channel, 0);
        sn.slf_cap = cap;
        if (sn.slf_res > 0 && sn.slf_cap > 0) {
            sn.slf_freq = 0.64 / (sn.slf_res * sn.slf_cap);
            //LOG(1,("SN76477 #%d: SLF freqency %f\n", chip, sn.slf_freq));
        } else {
            sn.slf_freq = 0;
        }
    }

    /**
     * ***************************************************************************
     * set ONESHOT resistor (pin 24)
     * ***************************************************************************
     */
    public static void SN76477_set_oneshot_res(int chip, double res) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM;
        if (res == sn.oneshot_res) {
            return;
        }
        sn.oneshot_res = res;
        sn.oneshot_time = 0.8 * sn.oneshot_res * sn.oneshot_cap;
        //LOG(1,("SN76477 #%d: ONE-SHOT time %fs\n", chip, sn.oneshot_time));
    }

    /**
     * ***************************************************************************
     * set ONESHOT capacitor (pin 23)
     * ***************************************************************************
     */
    public static void SN76477_set_oneshot_cap(int chip, double cap) {
        _SN76477 sn = sn76477[chip];

        //CHECK_CHIP_NUM;
        if (cap == sn.oneshot_cap) {
            return;
        }
        sn.oneshot_cap = cap;
        sn.oneshot_time = 0.8 * sn.oneshot_res * sn.oneshot_cap;
        //LOG(1,("SN76477 #%d: ONE-SHOT time %fs\n", chip, sn.oneshot_time));
    }

    /**
     * ***************************************************************************
     * mixer select 0 0 0 : VCO
     * ***************************************************************************
     */
    static void SN76477_update_0(int chip, ShortPtr buffer, int length) {
        _SN76477 sn = sn76477[chip];
        while (length-- != 0) {
            /**
             * **********************************
             * VCO voltage controlled oscilator min. freq = 0.64 / (r_vco *
             * c_vco) freq. range is approx. 10:1
             * **********************************
             */
            if (sn.vco_select != 0) {
                /* VCO is controlled by SLF */
                if (sn.slf_dir == 0) {
                    sn.slf_level -= sn.slf_freq * 2 * 5.0 / sn.samplerate;
                    if (sn.slf_level <= 0.0) {
                        sn.slf_level = 0.0;
                        sn.slf_dir = 1;
                    }
                } else if (sn.slf_dir == 1) {
                    sn.slf_level += sn.slf_freq * 2 * 5.0 / sn.samplerate;
                    if (sn.slf_level >= 5.0) {
                        sn.slf_level = 5.0;
                        sn.slf_dir = 0;
                    }
                }
                sn.vco_step = sn.vco_freq * sn.slf_level;
            } else {
                /* VCO is controlled by external voltage */
                sn.vco_step = sn.vco_freq * sn.vco_voltage;
            }
            sn.vco_count -= sn.vco_step;
            while (sn.vco_count <= 0) {
                sn.vco_count += sn.samplerate;
                sn.vco_out ^= 1;
            }
            /**
             * ***********************************
             * VOLUME adjust for attack/decay
             * ***********************************
             */
            sn.vol_count -= sn.vol_rate;
            if (sn.vol_count <= 0) {
                int n = -sn.vol_count / sn.samplerate + 1;
                /* number of steps */
                sn.vol_count += n * sn.samplerate;
                sn.vol += n * sn.vol_step;
                if (sn.vol < VMIN) {
                    sn.vol = VMIN;
                }
                if (sn.vol > VMAX) {
                    sn.vol = VMAX;
                }
                //LOG(3,("SN76477 #%d: vol = $%04X\n", chip, sn.vol));      
            }
            buffer.writeinc(sn.vco_out != 0 ? (short) sn.vol_lookup[sn.vol - VMIN] : (short) -sn.vol_lookup[sn.vol - VMIN]);
        }
    }

    /**
     * ***************************************************************************
     * mixer select 0 0 1 : SLF
     * ***************************************************************************
     */
    static void SN76477_update_1(int chip, ShortPtr buffer, int length) {
        _SN76477 sn = sn76477[chip];
        while (length-- != 0) {
            /**
             * ***********************************
             * SLF super low frequency oscillator frequency = 0.64 / (r_slf *
             * c_slf) ***********************************
             */
            sn.slf_count -= sn.slf_freq;
            while (sn.slf_count <= 0) {
                sn.slf_count += sn.samplerate;
                sn.slf_out ^= 1;
            }
            /**
             * ***********************************
             * VOLUME adjust for attack/decay
             * ***********************************
             */
            sn.vol_count -= sn.vol_rate;
            if (sn.vol_count <= 0) {
                int n = -sn.vol_count / sn.samplerate + 1;
                /* number of steps */
                sn.vol_count += n * sn.samplerate;
                sn.vol += n * sn.vol_step;
                if (sn.vol < VMIN) {
                    sn.vol = VMIN;
                }
                if (sn.vol > VMAX) {
                    sn.vol = VMAX;
                }
                //LOG(3,("SN76477 #%d: vol = $%04X\n", chip, sn.vol));      
            }
            buffer.writeinc(sn.slf_out != 0 ? (short) sn.vol_lookup[sn.vol - VMIN] : (short) -sn.vol_lookup[sn.vol - VMIN]);
        }
    }

    /**
     * ***************************************************************************
     * mixer select 0 1 0 : NOISE
     * ***************************************************************************
     */
    static void SN76477_update_2(int chip, ShortPtr buffer, int length) {
        _SN76477 sn = sn76477[chip];
        while (length-- != 0) {
            /**
             * ***********************************
             * NOISE pseudo rand number generator
             * ***********************************
             */
            if (sn.noise_res > 0) {
                sn.noise_poly = ((sn.noise_poly << 7)
                        + (sn.noise_poly >> 10)
                        + 0x18000) & 0x1ffff;
            }

            /* low pass filter: sample every noise_freq pseudo random value */
            sn.noise_count -= sn.noise_freq;
            while (sn.noise_count <= 0) {
                sn.noise_count = sn.samplerate;
                sn.noise_out = sn.noise_poly & 1;
            }
            /**
             * ***********************************
             * VOLUME adjust for attack/decay
             * ***********************************
             */
            sn.vol_count -= sn.vol_rate;
            if (sn.vol_count <= 0) {
                int n = -sn.vol_count / sn.samplerate + 1;
                /* number of steps */
                sn.vol_count += n * sn.samplerate;
                sn.vol += n * sn.vol_step;
                if (sn.vol < VMIN) {
                    sn.vol = VMIN;
                }
                if (sn.vol > VMAX) {
                    sn.vol = VMAX;
                }
                //LOG(3,("SN76477 #%d: vol = $%04X\n", chip, sn.vol));      
            }
            buffer.writeinc(sn.noise_out != 0 ? (short) sn.vol_lookup[sn.vol - VMIN] : (short) -sn.vol_lookup[sn.vol - VMIN]);
        }
    }

    /**
     * ***************************************************************************
     * mixer select 0 1 1 : VCO and NOISE
     * ***************************************************************************
     */
    static void SN76477_update_3(int chip, ShortPtr buffer, int length) {
        _SN76477 sn = sn76477[chip];
        while (length-- != 0) {
            /**
             * **********************************
             * VCO voltage controlled oscilator min. freq = 0.64 / (r_vco *
             * c_vco) freq. range is approx. 10:1
             * **********************************
             */
            if (sn.vco_select != 0) {
                /* VCO is controlled by SLF */
                if (sn.slf_dir == 0) {
                    sn.slf_level -= sn.slf_freq * 2 * 5.0 / sn.samplerate;
                    if (sn.slf_level <= 0.0) {
                        sn.slf_level = 0.0;
                        sn.slf_dir = 1;
                    }
                } else if (sn.slf_dir == 1) {
                    sn.slf_level += sn.slf_freq * 2 * 5.0 / sn.samplerate;
                    if (sn.slf_level >= 5.0) {
                        sn.slf_level = 5.0;
                        sn.slf_dir = 0;
                    }
                }
                sn.vco_step = sn.vco_freq * sn.slf_level;
            } else {
                /* VCO is controlled by external voltage */
                sn.vco_step = sn.vco_freq * sn.vco_voltage;
            }
            sn.vco_count -= sn.vco_step;
            while (sn.vco_count <= 0) {
                sn.vco_count += sn.samplerate;
                sn.vco_out ^= 1;
            }
            /**
             * ***********************************
             * NOISE pseudo rand number generator
             * ***********************************
             */
            if (sn.noise_res > 0) {
                sn.noise_poly = ((sn.noise_poly << 7)
                        + (sn.noise_poly >> 10)
                        + 0x18000) & 0x1ffff;
            }

            /* low pass filter: sample every noise_freq pseudo random value */
            sn.noise_count -= sn.noise_freq;
            while (sn.noise_count <= 0) {
                sn.noise_count = sn.samplerate;
                sn.noise_out = sn.noise_poly & 1;
            }
            /**
             * ***********************************
             * VOLUME adjust for attack/decay
             * ***********************************
             */
            sn.vol_count -= sn.vol_rate;
            if (sn.vol_count <= 0) {
                int n = -sn.vol_count / sn.samplerate + 1;
                /* number of steps */
                sn.vol_count += n * sn.samplerate;
                sn.vol += n * sn.vol_step;
                if (sn.vol < VMIN) {
                    sn.vol = VMIN;
                }
                if (sn.vol > VMAX) {
                    sn.vol = VMAX;
                }
                //LOG(3,("SN76477 #%d: vol = $%04X\n", chip, sn.vol));      
            }
            buffer.writeinc((sn.vco_out & sn.noise_out) != 0 ? (short) sn.vol_lookup[sn.vol - VMIN] : (short) -sn.vol_lookup[sn.vol - VMIN]);
        }
    }

    /**
     * ***************************************************************************
     * mixer select 1 0 0 : SLF and NOISE
     * ***************************************************************************
     */
    static void SN76477_update_4(int chip, ShortPtr buffer, int length) {
        _SN76477 sn = sn76477[chip];
        while (length-- != 0) {
            /**
             * ***********************************
             * SLF super low frequency oscillator frequency = 0.64 / (r_slf *
             * c_slf) ***********************************
             */
            sn.slf_count -= sn.slf_freq;
            while (sn.slf_count <= 0) {
                sn.slf_count += sn.samplerate;
                sn.slf_out ^= 1;
            }
            /**
             * ***********************************
             * NOISE pseudo rand number generator
             * ***********************************
             */
            if (sn.noise_res > 0) {
                sn.noise_poly = ((sn.noise_poly << 7)
                        + (sn.noise_poly >> 10)
                        + 0x18000) & 0x1ffff;
            }

            /* low pass filter: sample every noise_freq pseudo random value */
            sn.noise_count -= sn.noise_freq;
            while (sn.noise_count <= 0) {
                sn.noise_count = sn.samplerate;
                sn.noise_out = sn.noise_poly & 1;
            }
            /**
             * ***********************************
             * VOLUME adjust for attack/decay
             * ***********************************
             */
            sn.vol_count -= sn.vol_rate;
            if (sn.vol_count <= 0) {
                int n = -sn.vol_count / sn.samplerate + 1;
                /* number of steps */
                sn.vol_count += n * sn.samplerate;
                sn.vol += n * sn.vol_step;
                if (sn.vol < VMIN) {
                    sn.vol = VMIN;
                }
                if (sn.vol > VMAX) {
                    sn.vol = VMAX;
                }
                //LOG(3,("SN76477 #%d: vol = $%04X\n", chip, sn.vol));      
            }
            buffer.writeinc((sn.slf_out & sn.noise_out) != 0 ? (short) sn.vol_lookup[sn.vol - VMIN] : (short) -sn.vol_lookup[sn.vol - VMIN]);
        }
    }

    /**
     * ***************************************************************************
     * mixer select 1 0 1 : VCO, SLF and NOISE
     * ***************************************************************************
     */
    static void SN76477_update_5(int chip, ShortPtr buffer, int length) {
        _SN76477 sn = sn76477[chip];
        while (length-- != 0) {
            /**
             * ***********************************
             * SLF super low frequency oscillator frequency = 0.64 / (r_slf *
             * c_slf) ***********************************
             */
            sn.slf_count -= sn.slf_freq;
            while (sn.slf_count <= 0) {
                sn.slf_count += sn.samplerate;
                sn.slf_out ^= 1;
            }
            /**
             * **********************************
             * VCO voltage controlled oscilator min. freq = 0.64 / (r_vco *
             * c_vco) freq. range is approx. 10:1
             * **********************************
             */
            if (sn.vco_select != 0) {
                /* VCO is controlled by SLF */
                if (sn.slf_dir == 0) {
                    sn.slf_level -= sn.slf_freq * 2 * 5.0 / sn.samplerate;
                    if (sn.slf_level <= 0.0) {
                        sn.slf_level = 0.0;
                        sn.slf_dir = 1;
                    }
                } else if (sn.slf_dir == 1) {
                    sn.slf_level += sn.slf_freq * 2 * 5.0 / sn.samplerate;
                    if (sn.slf_level >= 5.0) {
                        sn.slf_level = 5.0;
                        sn.slf_dir = 0;
                    }
                }
                sn.vco_step = sn.vco_freq * sn.slf_level;
            } else {
                /* VCO is controlled by external voltage */
                sn.vco_step = sn.vco_freq * sn.vco_voltage;
            }
            sn.vco_count -= sn.vco_step;
            while (sn.vco_count <= 0) {
                sn.vco_count += sn.samplerate;
                sn.vco_out ^= 1;
            }
            /**
             * ***********************************
             * NOISE pseudo rand number generator
             * ***********************************
             */
            if (sn.noise_res > 0) {
                sn.noise_poly = ((sn.noise_poly << 7)
                        + (sn.noise_poly >> 10)
                        + 0x18000) & 0x1ffff;
            }

            /* low pass filter: sample every noise_freq pseudo random value */
            sn.noise_count -= sn.noise_freq;
            while (sn.noise_count <= 0) {
                sn.noise_count = sn.samplerate;
                sn.noise_out = sn.noise_poly & 1;
            }
            /**
             * ***********************************
             * VOLUME adjust for attack/decay
             * ***********************************
             */
            sn.vol_count -= sn.vol_rate;
            if (sn.vol_count <= 0) {
                int n = -sn.vol_count / sn.samplerate + 1;
                /* number of steps */
                sn.vol_count += n * sn.samplerate;
                sn.vol += n * sn.vol_step;
                if (sn.vol < VMIN) {
                    sn.vol = VMIN;
                }
                if (sn.vol > VMAX) {
                    sn.vol = VMAX;
                }
                //LOG(3,("SN76477 #%d: vol = $%04X\n", chip, sn.vol));      
            }
            buffer.writeinc((sn.vco_out & sn.slf_out & sn.noise_out) != 0 ? (short) sn.vol_lookup[sn.vol - VMIN] : (short) -sn.vol_lookup[sn.vol - VMIN]);
        }
    }

    /**
     * ***************************************************************************
     * mixer select 1 1 0 : VCO and SLF
     * ***************************************************************************
     */
    static void SN76477_update_6(int chip, ShortPtr buffer, int length) {
        _SN76477 sn = sn76477[chip];
        while (length-- != 0) {
            /**
             * ***********************************
             * SLF super low frequency oscillator frequency = 0.64 / (r_slf *
             * c_slf) ***********************************
             */
            sn.slf_count -= sn.slf_freq;
            while (sn.slf_count <= 0) {
                sn.slf_count += sn.samplerate;
                sn.slf_out ^= 1;
            }
            /**
             * **********************************
             * VCO voltage controlled oscilator min. freq = 0.64 / (r_vco *
             * c_vco) freq. range is approx. 10:1
             * **********************************
             */
            if (sn.vco_select != 0) {
                /* VCO is controlled by SLF */
                if (sn.slf_dir == 0) {
                    sn.slf_level -= sn.slf_freq * 2 * 5.0 / sn.samplerate;
                    if (sn.slf_level <= 0.0) {
                        sn.slf_level = 0.0;
                        sn.slf_dir = 1;
                    }
                } else if (sn.slf_dir == 1) {
                    sn.slf_level += sn.slf_freq * 2 * 5.0 / sn.samplerate;
                    if (sn.slf_level >= 5.0) {
                        sn.slf_level = 5.0;
                        sn.slf_dir = 0;
                    }
                }
                sn.vco_step = sn.vco_freq * sn.slf_level;
            } else {
                /* VCO is controlled by external voltage */
                sn.vco_step = sn.vco_freq * sn.vco_voltage;
            }
            sn.vco_count -= sn.vco_step;
            while (sn.vco_count <= 0) {
                sn.vco_count += sn.samplerate;
                sn.vco_out ^= 1;
            }
            /**
             * ***********************************
             * VOLUME adjust for attack/decay
             * ***********************************
             */
            sn.vol_count -= sn.vol_rate;
            if (sn.vol_count <= 0) {
                int n = -sn.vol_count / sn.samplerate + 1;
                /* number of steps */
                sn.vol_count += n * sn.samplerate;
                sn.vol += n * sn.vol_step;
                if (sn.vol < VMIN) {
                    sn.vol = VMIN;
                }
                if (sn.vol > VMAX) {
                    sn.vol = VMAX;
                }
                //LOG(3,("SN76477 #%d: vol = $%04X\n", chip, sn.vol));      
            }
            buffer.writeinc((sn.vco_out & sn.slf_out) != 0 ? (short) sn.vol_lookup[sn.vol - VMIN] : (short) -sn.vol_lookup[sn.vol - VMIN]);
        }
    }

    /**
     * ***************************************************************************
     * mixer select 1 1 1 : Inhibit
     * ***************************************************************************
     */
    static void SN76477_update_7(int chip, ShortPtr buffer, int length) {
        while (length-- != 0) {
            buffer.writeinc((short) 0);
        }
    }
    public static StreamInitPtr SN76477_sound_update = new StreamInitPtr() {
        public void handler(int param, ShortPtr buffer, int length) {
            _SN76477 sn = sn76477[param];
            if (sn.enable != 0) {
                SN76477_update_7(param, buffer, length);
            } else {
                switch (sn.mixer) {
                    case 0:
                        SN76477_update_0(param, buffer, length);
                        break;
                    case 1:
                        SN76477_update_1(param, buffer, length);
                        break;
                    case 2:
                        SN76477_update_2(param, buffer, length);
                        break;
                    case 3:
                        SN76477_update_3(param, buffer, length);
                        break;
                    case 4:
                        SN76477_update_4(param, buffer, length);
                        break;
                    case 5:
                        SN76477_update_5(param, buffer, length);
                        break;
                    case 6:
                        SN76477_update_6(param, buffer, length);
                        break;
                    default:
                        SN76477_update_7(param, buffer, length);
                        break;
                }
            }
        }
    };

    @Override
    public int chips_num(MachineSound msound) {
        return ((SN76477interface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return 0;
    }

    @Override
    public int start(MachineSound msound) {
        int i;
        intf = (SN76477interface) msound.sound_interface;

        for (i = 0; i < intf.num; i++) {
            String name;

            sn76477[i] = new _SN76477();
            if (sn76477[i] == null) {
                // LOG(0, ("%s failed to malloc struct SN76477\n", name));
                return 1;
            }
            //memset(sn76477[i], 0, sizeof(struct SN76477));

            name = sprintf("SN76477 #%d", i);
            sn76477[i].channel = stream_init(name, intf.mixing_level[i], Machine.sample_rate, i, SN76477_sound_update);
            if (sn76477[i].channel == -1) {
                //LOG(0, ("%s stream_init failed\n", name));
                return 1;
            }
            sn76477[i].samplerate = Machine.sample_rate != 0 ? Machine.sample_rate : 1;
            /* set up interface (default) values */
            SN76477_set_noise_res(i, intf.noise_res[i]);
            SN76477_set_filter_res(i, intf.filter_res[i]);
            SN76477_set_filter_cap(i, intf.filter_cap[i]);
            SN76477_set_decay_res(i, intf.decay_res[i]);
            SN76477_set_attack_decay_cap(i, intf.attack_decay_cap[i]);
            SN76477_set_attack_res(i, intf.attack_res[i]);
            SN76477_set_amplitude_res(i, intf.amplitude_res[i]);
            SN76477_set_feedback_res(i, intf.feedback_res[i]);
            SN76477_set_oneshot_res(i, intf.oneshot_res[i]);
            SN76477_set_oneshot_cap(i, intf.oneshot_cap[i]);
            SN76477_set_pitch_voltage(i, intf.pitch_voltage[i]);
            SN76477_set_slf_res(i, intf.slf_res[i]);
            SN76477_set_slf_cap(i, intf.slf_cap[i]);
            SN76477_set_vco_res(i, intf.vco_res[i]);
            SN76477_set_vco_cap(i, intf.vco_cap[i]);
            SN76477_set_vco_voltage(i, intf.vco_voltage[i]);
            SN76477_mixer_w(i, 0x07);
            /* turn off mixing */
            SN76477_envelope_w(i, 0x03);
            /* envelope inputs open */
            SN76477_enable_w(i, 0x01);
            /* enable input open */
        }
        return 0;
    }

    @Override
    public void stop() {
        int i;
        for (i = 0; i < intf.num; i++) {
            if (sn76477[i] != null) {
                if (sn76477[i].envelope_timer != null) {
                    timer_remove(sn76477[i].envelope_timer);
                }
                sn76477[i] = null;
            }
            sn76477[i] = null;
        }
    }

    @Override
    public void update() {
        int i;
        for (i = 0; i < intf.num; i++) {
            stream_update(i, 0);
        }
    }

    @Override
    public void reset() {
        //no functionality expected
    }

}
