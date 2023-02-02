/*
 * ported to v0.37b7 
 */
/**
 * Changelog
 * =========
 * 02/02/2023 - shadow - This file should be complete for 0.37b7 version
 */
package arcadeflex.v037b7.sound;

public class sn76477H {

    public static int MAX_SN76477 = 4;

    /* Little helpers for magnitude conversions */
    public static double RES_K(double res) {
        return ((double) res * 1e3);
    }

    public static double RES_M(double res) {
        return ((double) res * 1e6);
    }

    public static double CAP_U(double cap) {
        return ((double) cap * 1e-6);
    }

    public static double CAP_N(double cap) {
        return ((double) cap * 1e-9);
    }

    public static double CAP_P(double cap) {
        return ((double) cap * 1e-12);
    }

    /* The interface structure */
    public static class SN76477interface {

        public SN76477interface(int num, int[] mixing_level, double[] noise_res, double[] filter_res, double[] filter_cap, double[] decay_res, double[] attack_decay_cap,
                double[] attack_res, double[] amplitude_res, double[] feedback_res, double[] vco_voltage, double[] vco_cap, double[] vco_res,
                double[] pitch_voltage, double[] slf_res, double[] slf_cap, double[] oneshot_cap, double[] oneshot_res) {
            this.num = num;
            this.mixing_level = mixing_level;
            this.noise_res = noise_res;
            this.filter_res = filter_res;
            this.filter_cap = filter_cap;
            this.decay_res = decay_res;
            this.attack_decay_cap = attack_decay_cap;
            this.attack_res = attack_res;
            this.amplitude_res = amplitude_res;
            this.feedback_res = feedback_res;
            this.vco_voltage = vco_voltage;
            this.vco_cap = vco_cap;
            this.vco_res = vco_res;
            this.pitch_voltage = pitch_voltage;
            this.slf_res = slf_res;
            this.slf_cap = slf_cap;
            this.oneshot_cap = oneshot_cap;
            this.oneshot_res = oneshot_res;

        }
        int num;
        int[] mixing_level;//[MAX_SN76477];
        double[] noise_res;//[MAX_SN76477];
        double[] filter_res;//[MAX_SN76477];
        double[] filter_cap;//[MAX_SN76477];
        double[] decay_res;//[MAX_SN76477];
        double[] attack_decay_cap;//[MAX_SN76477];
        double[] attack_res;//[MAX_SN76477];
        double[] amplitude_res;//[MAX_SN76477];
        double[] feedback_res;//[MAX_SN76477];
        double[] vco_voltage;//[MAX_SN76477];
        double[] vco_cap;//[MAX_SN76477];
        double[] vco_res;//[MAX_SN76477];
        double[] pitch_voltage;//[MAX_SN76477];
        double[] slf_res;//[MAX_SN76477];
        double[] slf_cap;//[MAX_SN76477];
        double[] oneshot_cap;//[MAX_SN76477];
        double[] oneshot_res;//[MAX_SN76477];
    }
}
