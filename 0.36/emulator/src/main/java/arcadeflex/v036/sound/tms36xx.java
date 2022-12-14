/**
 * ported to 0.36
 */
package arcadeflex.v036.sound;

import gr.codebb.arcadeflex.common.PtrLib.ShortPtr;
import static common.libc.cstring.memset;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fprintf;
import static gr.codebb.arcadeflex.v036.platform.libc_old.sprintf;
import arcadeflex.v036.sound.streams.StreamInitPtr;
import static arcadeflex.v036.sound.streams.stream_init;
import static arcadeflex.v036.sound.streams.stream_update;
import static arcadeflex.v036.sound.tms36xxH.*;

public class tms36xx extends snd_interface {

    public static int VMIN = 0x0000;
    public static int VMAX = 0x7fff;

    /* the frequencies are later adjusted by "* clock / FSCALE" */
    public static int FSCALE = 1024;

    public class TMS36XX {

        String subtype;
        /* subtype name MM6221AA, TMS3615 or TMS3617 */
        int channel;
        /* returned by stream_init() */

        int samplerate;
        /* from Machine.sample_rate */

        int basefreq;
        /* chip's base frequency */
        int octave;
        /* octave select of the TMS3615 */

        int speed;
        /* speed of the tune */
        int tune_counter;
        /* tune counter */
        int note_counter;
        /* note counter */

        int voices;
        /* active voices */
        int shift;
        /* shift toggles between 0 and 6 to allow decaying voices */
        int[] vol = new int[12];
        /* (decaying) volume of harmonics notes */
        int[] vol_counter = new int[12];/* volume adjustment counter */
        int[] decay = new int[12];
        /* volume adjustment rate - dervied from decay */

        int[] counter = new int[12];
        /* tone frequency counter */
        int[] frequency = new int[12];
        /* tone frequency */
        int output;
        /* output signal bits */
        int enable;
        /* mask which harmoics */

        int tune_num;
        /* tune currently playing */
        int tune_ofs;
        /* note currently playing */
        int tune_max;
        /* end of tune */
    }
    static TMS36XXinterface intf;
    static TMS36XX[] tms36xx = new TMS36XX[MAX_TMS36XX];

    public static int C(int n) {
        return (int) ((FSCALE << (n - 1)) * 1.18921);
    }

    /* 2^(3/12) */
    public static int Cx(int n) {
        return (int) ((FSCALE << (n - 1)) * 1.25992);
    }

    /* 2^(4/12) */
    public static int D(int n) {
        return (int) ((FSCALE << (n - 1)) * 1.33484);
    }

    /* 2^(5/12) */
    public static int Dx(int n) {
        return (int) ((FSCALE << (n - 1)) * 1.41421);
    }

    /* 2^(6/12) */
    public static int E(int n) {
        return (int) ((FSCALE << (n - 1)) * 1.49831);
    }

    /* 2^(7/12) */
    public static int F(int n) {
        return (int) ((FSCALE << (n - 1)) * 1.58740);
    }

    /* 2^(8/12) */
    public static int Fx(int n) {
        return (int) ((FSCALE << (n - 1)) * 1.68179);
    }

    /* 2^(9/12) */
    public static int G(int n) {
        return (int) ((FSCALE << (n - 1)) * 1.78180);
    }

    /* 2^(10/12) */
    public static int Gx(int n) {
        return (int) ((FSCALE << (n - 1)) * 1.88775);
    }

    /* 2^(11/12) */
    public static int A(int n) {
        return (int) ((FSCALE << n));
    }

    /* A */
    public static int Ax(int n) {
        return (int) ((FSCALE << n) * 1.05946);
    }

    /* 2^(1/12) */
    public static int B(int n) {
        return (int) ((FSCALE << n) * 1.12246);
    }

    /* 2^(2/12) */
 /*
	 * Alarm sound?
	 * It is unknown what this sound is like. Until somebody manages
	 * trigger sound #1 of the Phoenix PCB sound chip I put just something
	 * 'alarming' in here.
     */
    static int tune1[] = {
        C(3), 0, 0, C(2), 0, 0,
        G(3), 0, 0, 0, 0, 0,
        C(3), 0, 0, 0, 0, 0,
        G(3), 0, 0, 0, 0, 0,
        C(3), 0, 0, 0, 0, 0,
        G(3), 0, 0, 0, 0, 0,
        C(3), 0, 0, 0, 0, 0,
        G(3), 0, 0, 0, 0, 0,
        C(3), 0, 0, C(4), 0, 0,
        G(3), 0, 0, 0, 0, 0,
        C(3), 0, 0, 0, 0, 0,
        G(3), 0, 0, 0, 0, 0,
        C(3), 0, 0, 0, 0, 0,
        G(3), 0, 0, 0, 0, 0,
        C(3), 0, 0, 0, 0, 0,
        G(3), 0, 0, 0, 0, 0,
        C(3), 0, 0, C(2), 0, 0,
        G(3), 0, 0, 0, 0, 0,
        C(3), 0, 0, 0, 0, 0,
        G(3), 0, 0, 0, 0, 0,
        C(3), 0, 0, 0, 0, 0,
        G(3), 0, 0, 0, 0, 0,
        C(3), 0, 0, 0, 0, 0,
        G(3), 0, 0, 0, 0, 0,
        C(3), 0, 0, C(4), 0, 0,
        G(3), 0, 0, 0, 0, 0,
        C(3), 0, 0, 0, 0, 0,
        G(3), 0, 0, 0, 0, 0,
        C(3), 0, 0, 0, 0, 0,
        G(3), 0, 0, 0, 0, 0,
        C(3), 0, 0, 0, 0, 0,
        G(3), 0, 0, 0, 0, 0,};
    /*
	 * Fuer Elise, Beethoven
	 * (Excuse my non-existent musical skill, Mr. B ;-)
     */
    static int tune2[] = {
        D(3), D(4), D(5), 0, 0, 0,
        Cx(3), Cx(4), Cx(5), 0, 0, 0,
        D(3), D(4), D(5), 0, 0, 0,
        Cx(3), Cx(4), Cx(5), 0, 0, 0,
        D(3), D(4), D(5), 0, 0, 0,
        A(2), A(3), A(4), 0, 0, 0,
        C(3), C(4), C(5), 0, 0, 0,
        Ax(2), Ax(3), Ax(4), 0, 0, 0,
        G(2), G(3), G(4), 0, 0, 0,
        D(1), D(2), D(3), 0, 0, 0,
        G(1), G(2), G(3), 0, 0, 0,
        Ax(1), Ax(2), Ax(3), 0, 0, 0,
        D(2), D(3), D(4), 0, 0, 0,
        G(2), G(3), G(4), 0, 0, 0,
        A(2), A(3), A(4), 0, 0, 0,
        D(1), D(2), D(3), 0, 0, 0,
        A(1), A(2), A(3), 0, 0, 0,
        D(2), D(3), D(4), 0, 0, 0,
        Fx(2), Fx(3), Fx(4), 0, 0, 0,
        A(2), A(3), A(4), 0, 0, 0,
        Ax(2), Ax(3), Ax(4), 0, 0, 0,
        D(1), D(2), D(3), 0, 0, 0,
        G(1), G(2), G(3), 0, 0, 0,
        Ax(1), Ax(2), Ax(3), 0, 0, 0,
        D(3), D(4), D(5), 0, 0, 0,
        Cx(3), Cx(4), Cx(5), 0, 0, 0,
        D(3), D(4), D(5), 0, 0, 0,
        Cx(3), Cx(4), Cx(5), 0, 0, 0,
        D(3), D(4), D(5), 0, 0, 0,
        A(2), A(3), A(4), 0, 0, 0,
        C(3), C(4), C(5), 0, 0, 0,
        Ax(2), Ax(3), Ax(4), 0, 0, 0,
        G(2), G(3), G(4), 0, 0, 0,
        D(1), D(2), D(3), 0, 0, 0,
        G(1), G(2), G(3), 0, 0, 0,
        Ax(1), Ax(2), Ax(3), 0, 0, 0,
        D(2), D(3), D(4), 0, 0, 0,
        G(2), G(3), G(4), 0, 0, 0,
        A(2), A(3), A(4), 0, 0, 0,
        D(1), D(2), D(3), 0, 0, 0,
        A(1), A(2), A(3), 0, 0, 0,
        D(2), D(3), D(4), 0, 0, 0,
        Ax(2), Ax(3), Ax(4), 0, 0, 0,
        A(2), A(3), A(4), 0, 0, 0,
        0, 0, 0, G(2), G(3), G(4),
        D(1), D(2), D(3), 0, 0, 0,
        G(1), G(2), G(3), 0, 0, 0,
        0, 0, 0, 0, 0, 0
    };
    /*
	 * The theme from Phoenix, a sad little tune.
	 * Gerald Coy:
	 *	 The starting song from Phoenix is coming from a old french movie and
	 *	 it's called : "Jeux interdits" which means "unallowed games"  ;-)
	 * Mirko Buffoni:
	 *	 It's called "Sogni proibiti" in italian, by Anonymous.
	 * Magic*:
	 *	 This song is a classical piece called "ESTUDIO" from M.A.Robira.
     */
    static int tune3[] = {
        A(2), A(3), A(4), D(1), D(2), D(3),
        0, 0, 0, 0, 0, 0,
        A(2), A(3), A(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        A(2), A(3), A(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        A(2), A(3), A(4), A(1), A(2), A(3),
        0, 0, 0, 0, 0, 0,
        G(2), G(3), G(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        F(2), F(3), F(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        F(2), F(3), F(4), F(1), F(2), F(3),
        0, 0, 0, 0, 0, 0,
        E(2), E(3), E(4), F(1), F(2), F(3),
        0, 0, 0, 0, 0, 0,
        D(2), D(3), D(4), F(1), F(2), F(3),
        0, 0, 0, 0, 0, 0,
        D(2), D(3), D(4), A(1), A(2), A(3),
        0, 0, 0, 0, 0, 0,
        F(2), F(3), F(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        A(2), A(3), A(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        D(3), D(4), D(5), D(1), D(2), D(3),
        0, 0, 0, 0, 0, 0,
        0, 0, 0, D(1), D(2), D(3),
        0, 0, 0, F(1), F(2), F(3),
        0, 0, 0, A(1), A(2), A(3),
        0, 0, 0, D(2), D(2), D(2),
        D(3), D(4), D(5), D(1), D(2), D(3),
        0, 0, 0, 0, 0, 0,
        C(3), C(4), C(5), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        Ax(2), Ax(3), Ax(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        Ax(2), Ax(3), Ax(4), Ax(1), Ax(2), Ax(3),
        0, 0, 0, 0, 0, 0,
        A(2), A(3), A(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        G(2), G(3), G(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        G(2), G(3), G(4), G(1), G(2), G(3),
        0, 0, 0, 0, 0, 0,
        A(2), A(3), A(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        Ax(2), Ax(3), Ax(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        A(2), A(3), A(4), A(1), A(2), A(3),
        0, 0, 0, 0, 0, 0,
        Ax(2), Ax(3), Ax(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        A(2), A(3), A(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        Cx(3), Cx(4), Cx(5), A(1), A(2), A(3),
        0, 0, 0, 0, 0, 0,
        Ax(2), Ax(3), Ax(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        A(2), A(3), A(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        A(2), A(3), A(4), F(1), F(2), F(3),
        0, 0, 0, 0, 0, 0,
        G(2), G(3), G(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        F(2), F(3), F(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        F(2), F(3), F(4), D(1), D(2), D(3),
        0, 0, 0, 0, 0, 0,
        E(2), E(3), E(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        D(2), D(3), D(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        E(2), E(3), E(4), E(1), E(2), E(3),
        0, 0, 0, 0, 0, 0,
        E(2), E(3), E(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        E(2), E(3), E(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        E(2), E(3), E(4), Ax(1), Ax(2), Ax(3),
        0, 0, 0, 0, 0, 0,
        F(2), F(3), F(4), 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        E(2), E(3), E(4), F(1), F(2), F(3),
        0, 0, 0, 0, 0, 0,
        D(2), D(3), D(4), D(1), D(2), D(3),
        0, 0, 0, 0, 0, 0,
        F(2), F(3), F(4), A(1), A(2), A(3),
        0, 0, 0, 0, 0, 0,
        A(2), A(3), A(4), F(1), F(2), F(3),
        0, 0, 0, 0, 0, 0,
        D(3), D(4), D(5), D(1), D(2), D(3),
        0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0
    };
    /* This is used to play single notes for the TMS3615/TMS3617 */
    static int tune4[] = {
        /*	16'     8'      5 1/3'  4'      2 2/3'  2'      */
        B(0), B(1), Dx(2), B(2), Dx(3), B(3),
        C(1), C(2), E(2), C(3), E(3), C(4),
        Cx(1), Cx(2), F(2), Cx(3), F(3), Cx(4),
        D(1), D(2), Fx(2), D(3), Fx(3), D(4),
        Dx(1), Dx(2), G(2), Dx(3), G(3), Dx(4),
        E(1), E(2), Gx(2), E(3), Gx(3), E(4),
        F(1), F(2), A(2), F(3), A(3), F(4),
        Fx(1), Fx(2), Ax(2), Fx(3), Ax(3), Fx(4),
        G(1), G(2), B(2), G(3), B(3), G(4),
        Gx(1), Gx(2), C(3), Gx(3), C(4), Gx(4),
        A(1), A(2), Cx(3), A(3), Cx(4), A(4),
        Ax(1), Ax(2), D(3), Ax(3), D(4), Ax(4),
        B(1), B(2), Dx(3), B(3), Dx(4), B(4)
    };
    static int tunes[][] = {null, tune1, tune2, tune3, tune4};

    public static StreamInitPtr tms36xx_sound_update = new StreamInitPtr() {
        public void handler(int param, ShortPtr buffer, int length) {
            TMS36XX tms = tms36xx[param];
            int samplerate = tms.samplerate;

            /* no tune played? */
            if (tunes[tms.tune_num] == null || tms.voices == 0) {
                while (--length >= 0) {
                    buffer.write(length, (short) 0);
                }
                return;
            }

            while (length-- > 0) {
                int sum = 0;

                /* decay the twelve voices */
                if (tms.vol[0] > VMIN) {
                    /* decay of first voice */
                    tms.vol_counter[0] -= tms.decay[0];
                    while (tms.vol_counter[0] <= 0) {
                        tms.vol_counter[0] += samplerate;
                        if (tms.vol[0]-- <= VMIN) {
                            tms.frequency[0] = 0;
                            tms.vol[0] = VMIN;
                            break;
                        }
                    }
                }
                if (tms.vol[1] > VMIN) {
                    /* decay of first voice */
                    tms.vol_counter[1] -= tms.decay[1];
                    while (tms.vol_counter[1] <= 0) {
                        tms.vol_counter[1] += samplerate;
                        if (tms.vol[1]-- <= VMIN) {
                            tms.frequency[1] = 0;
                            tms.vol[1] = VMIN;
                            break;
                        }
                    }
                }
                if (tms.vol[2] > VMIN) {
                    /* decay of first voice */
                    tms.vol_counter[2] -= tms.decay[2];
                    while (tms.vol_counter[2] <= 0) {
                        tms.vol_counter[2] += samplerate;
                        if (tms.vol[2]-- <= VMIN) {
                            tms.frequency[2] = 0;
                            tms.vol[2] = VMIN;
                            break;
                        }
                    }
                }
                if (tms.vol[3] > VMIN) {
                    /* decay of first voice */
                    tms.vol_counter[3] -= tms.decay[3];
                    while (tms.vol_counter[3] <= 0) {
                        tms.vol_counter[3] += samplerate;
                        if (tms.vol[3]-- <= VMIN) {
                            tms.frequency[3] = 0;
                            tms.vol[3] = VMIN;
                            break;
                        }
                    }
                }
                if (tms.vol[4] > VMIN) {
                    /* decay of first voice */
                    tms.vol_counter[4] -= tms.decay[4];
                    while (tms.vol_counter[4] <= 0) {
                        tms.vol_counter[4] += samplerate;
                        if (tms.vol[4]-- <= VMIN) {
                            tms.frequency[4] = 0;
                            tms.vol[4] = VMIN;
                            break;
                        }
                    }
                }
                if (tms.vol[5] > VMIN) {
                    /* decay of first voice */
                    tms.vol_counter[5] -= tms.decay[5];
                    while (tms.vol_counter[5] <= 0) {
                        tms.vol_counter[5] += samplerate;
                        if (tms.vol[5]-- <= VMIN) {
                            tms.frequency[5] = 0;
                            tms.vol[5] = VMIN;
                            break;
                        }
                    }
                }
                if (tms.vol[6] > VMIN) {
                    /* decay of first voice */
                    tms.vol_counter[6] -= tms.decay[6];
                    while (tms.vol_counter[6] <= 0) {
                        tms.vol_counter[6] += samplerate;
                        if (tms.vol[6]-- <= VMIN) {
                            tms.frequency[6] = 0;
                            tms.vol[6] = VMIN;
                            break;
                        }
                    }
                }
                if (tms.vol[7] > VMIN) {
                    /* decay of first voice */
                    tms.vol_counter[7] -= tms.decay[7];
                    while (tms.vol_counter[7] <= 0) {
                        tms.vol_counter[7] += samplerate;
                        if (tms.vol[7]-- <= VMIN) {
                            tms.frequency[7] = 0;
                            tms.vol[7] = VMIN;
                            break;
                        }
                    }
                }
                if (tms.vol[8] > VMIN) {
                    /* decay of first voice */
                    tms.vol_counter[8] -= tms.decay[8];
                    while (tms.vol_counter[8] <= 0) {
                        tms.vol_counter[8] += samplerate;
                        if (tms.vol[8]-- <= VMIN) {
                            tms.frequency[8] = 0;
                            tms.vol[8] = VMIN;
                            break;
                        }
                    }
                }
                if (tms.vol[9] > VMIN) {
                    /* decay of first voice */
                    tms.vol_counter[9] -= tms.decay[9];
                    while (tms.vol_counter[9] <= 0) {
                        tms.vol_counter[9] += samplerate;
                        if (tms.vol[9]-- <= VMIN) {
                            tms.frequency[9] = 0;
                            tms.vol[9] = VMIN;
                            break;
                        }
                    }
                }
                if (tms.vol[10] > VMIN) {
                    /* decay of first voice */
                    tms.vol_counter[10] -= tms.decay[10];
                    while (tms.vol_counter[10] <= 0) {
                        tms.vol_counter[10] += samplerate;
                        if (tms.vol[10]-- <= VMIN) {
                            tms.frequency[10] = 0;
                            tms.vol[10] = VMIN;
                            break;
                        }
                    }
                }
                if (tms.vol[11] > VMIN) {
                    /* decay of first voice */
                    tms.vol_counter[11] -= tms.decay[11];
                    while (tms.vol_counter[11] <= 0) {
                        tms.vol_counter[11] += samplerate;
                        if (tms.vol[11]-- <= VMIN) {
                            tms.frequency[11] = 0;
                            tms.vol[11] = VMIN;
                            break;
                        }
                    }
                }
                tms.tune_counter -= tms.speed;
                if (tms.tune_counter <= 0) {
                    int n = (-tms.tune_counter / samplerate) + 1;
                    tms.tune_counter += n * samplerate;

                    if ((tms.note_counter -= n) <= 0) {
                        tms.note_counter += VMAX;
                        if (tms.tune_ofs < tms.tune_max) {
                            /* shift to the other 'bank' of voices */
                            tms.shift ^= 6;
                            /* restart one 'bank' of voices */
                            if (tunes[tms.tune_num][tms.tune_ofs * 6 + 0] != 0) {
                                tms.frequency[tms.shift + 0]
                                        = tunes[tms.tune_num][tms.tune_ofs * 6 + 0]
                                        * (tms.basefreq << tms.octave) / FSCALE;
                                tms.vol[tms.shift + 0] = VMAX;
                            }
                            if (tunes[tms.tune_num][tms.tune_ofs * 6 + 1] != 0) {
                                tms.frequency[tms.shift + 1]
                                        = tunes[tms.tune_num][tms.tune_ofs * 6 + 1]
                                        * (tms.basefreq << tms.octave) / FSCALE;
                                tms.vol[tms.shift + 1] = VMAX;
                            }
                            if (tunes[tms.tune_num][tms.tune_ofs * 6 + 2] != 0) {
                                tms.frequency[tms.shift + 2]
                                        = tunes[tms.tune_num][tms.tune_ofs * 6 + 2]
                                        * (tms.basefreq << tms.octave) / FSCALE;
                                tms.vol[tms.shift + 2] = VMAX;
                            }
                            if (tunes[tms.tune_num][tms.tune_ofs * 6 + 3] != 0) {
                                tms.frequency[tms.shift + 3]
                                        = tunes[tms.tune_num][tms.tune_ofs * 6 + 3]
                                        * (tms.basefreq << tms.octave) / FSCALE;
                                tms.vol[tms.shift + 3] = VMAX;
                            }
                            if (tunes[tms.tune_num][tms.tune_ofs * 6 + 4] != 0) {
                                tms.frequency[tms.shift + 4]
                                        = tunes[tms.tune_num][tms.tune_ofs * 6 + 4]
                                        * (tms.basefreq << tms.octave) / FSCALE;
                                tms.vol[tms.shift + 4] = VMAX;
                            }
                            if (tunes[tms.tune_num][tms.tune_ofs * 6 + 5] != 0) {
                                tms.frequency[tms.shift + 5]
                                        = tunes[tms.tune_num][tms.tune_ofs * 6 + 5]
                                        * (tms.basefreq << tms.octave) / FSCALE;
                                tms.vol[tms.shift + 5] = VMAX;
                            }
                            tms.tune_ofs++;
                        }
                    }
                }

                /* update the twelve voices */
                if ((tms.enable & (1 << 0)) != 0 && tms.frequency[0] != 0) {
                    /* first note */
                    tms.counter[0] -= tms.frequency[0];
                    while (tms.counter[0] <= 0) {
                        tms.counter[0] += samplerate;
                        tms.output ^= 1 << 0;
                    }
                    if ((tms.output & tms.enable & (1 << 0)) != 0) {
                        sum += tms.vol[0];
                    }
                }
                if ((tms.enable & (1 << 1)) != 0 && tms.frequency[1] != 0) {
                    /* first note */
                    tms.counter[1] -= tms.frequency[1];
                    while (tms.counter[1] <= 0) {
                        tms.counter[1] += samplerate;
                        tms.output ^= 1 << 1;
                    }
                    if ((tms.output & tms.enable & (1 << 1)) != 0) {
                        sum += tms.vol[1];
                    }
                }
                if ((tms.enable & (1 << 2)) != 0 && tms.frequency[2] != 0) {
                    /* first note */
                    tms.counter[2] -= tms.frequency[2];
                    while (tms.counter[2] <= 0) {
                        tms.counter[2] += samplerate;
                        tms.output ^= 1 << 2;
                    }
                    if ((tms.output & tms.enable & (1 << 2)) != 0) {
                        sum += tms.vol[2];
                    }
                }
                if ((tms.enable & (1 << 3)) != 0 && tms.frequency[3] != 0) {
                    /* first note */
                    tms.counter[3] -= tms.frequency[3];
                    while (tms.counter[3] <= 0) {
                        tms.counter[3] += samplerate;
                        tms.output ^= 1 << 3;
                    }
                    if ((tms.output & tms.enable & (1 << 3)) != 0) {
                        sum += tms.vol[3];
                    }
                }
                if ((tms.enable & (1 << 4)) != 0 && tms.frequency[4] != 0) {
                    /* first note */
                    tms.counter[4] -= tms.frequency[4];
                    while (tms.counter[4] <= 0) {
                        tms.counter[4] += samplerate;
                        tms.output ^= 1 << 4;
                    }
                    if ((tms.output & tms.enable & (1 << 4)) != 0) {
                        sum += tms.vol[4];
                    }
                }
                if ((tms.enable & (1 << 5)) != 0 && tms.frequency[5] != 0) {
                    /* first note */
                    tms.counter[5] -= tms.frequency[5];
                    while (tms.counter[5] <= 0) {
                        tms.counter[5] += samplerate;
                        tms.output ^= 1 << 5;
                    }
                    if ((tms.output & tms.enable & (1 << 5)) != 0) {
                        sum += tms.vol[5];
                    }
                }
                if ((tms.enable & (1 << 6)) != 0 && tms.frequency[6] != 0) {
                    /* first note */
                    tms.counter[6] -= tms.frequency[6];
                    while (tms.counter[6] <= 0) {
                        tms.counter[6] += samplerate;
                        tms.output ^= 1 << 6;
                    }
                    if ((tms.output & tms.enable & (1 << 6)) != 0) {
                        sum += tms.vol[6];
                    }
                }
                if ((tms.enable & (1 << 7)) != 0 && tms.frequency[7] != 0) {
                    /* first note */
                    tms.counter[7] -= tms.frequency[7];
                    while (tms.counter[7] <= 0) {
                        tms.counter[7] += samplerate;
                        tms.output ^= 1 << 7;
                    }
                    if ((tms.output & tms.enable & (1 << 7)) != 0) {
                        sum += tms.vol[7];
                    }
                }
                if ((tms.enable & (1 << 8)) != 0 && tms.frequency[8] != 0) {
                    /* first note */
                    tms.counter[8] -= tms.frequency[8];
                    while (tms.counter[8] <= 0) {
                        tms.counter[8] += samplerate;
                        tms.output ^= 1 << 8;
                    }
                    if ((tms.output & tms.enable & (1 << 8)) != 0) {
                        sum += tms.vol[8];
                    }
                }
                if ((tms.enable & (1 << 9)) != 0 && tms.frequency[9] != 0) {
                    /* first note */
                    tms.counter[9] -= tms.frequency[9];
                    while (tms.counter[9] <= 0) {
                        tms.counter[9] += samplerate;
                        tms.output ^= 1 << 9;
                    }
                    if ((tms.output & tms.enable & (1 << 9)) != 0) {
                        sum += tms.vol[9];
                    }
                }
                if ((tms.enable & (1 << 10)) != 0 && tms.frequency[10] != 0) {
                    /* first note */
                    tms.counter[10] -= tms.frequency[10];
                    while (tms.counter[10] <= 0) {
                        tms.counter[10] += samplerate;
                        tms.output ^= 1 << 10;
                    }
                    if ((tms.output & tms.enable & (1 << 10)) != 0) {
                        sum += tms.vol[10];
                    }
                }
                if ((tms.enable & (1 << 11)) != 0 && tms.frequency[11] != 0) {
                    /* first note */
                    tms.counter[11] -= tms.frequency[11];
                    while (tms.counter[11] <= 0) {
                        tms.counter[11] += samplerate;
                        tms.output ^= 1 << 11;
                    }
                    if ((tms.output & tms.enable & (1 << 11)) != 0) {
                        sum += tms.vol[11];
                    }
                }
                buffer.writeinc((short) (sum / tms.voices));
            }
        }
    };

    public static void tms36xx_reset_counters(int chip) {
        TMS36XX tms = tms36xx[chip];
        tms.tune_counter = 0;
        tms.note_counter = 0;
        memset(tms.vol_counter, 0, tms.vol_counter.length);
        memset(tms.counter, 0, tms.counter.length);
    }

    public static void mm6221aa_tune_w(int chip, int tune) {
        TMS36XX tms = tms36xx[chip];

        /* which tune? */
        tune &= 3;
        if (tune == tms.tune_num) {
            return;
        }

        //LOG(("%s tune:%X\n", tms.subtype, tune));
        /* update the stream before changing the tune */
        stream_update(tms.channel, 0);

        tms.tune_num = tune;
        tms.tune_ofs = 0;
        tms.tune_max = 96;
        /* fixed for now */
    }

    public static void tms36xx_note_w(int chip, int octave, int note) {
        TMS36XX tms = tms36xx[chip];

        octave &= 3;
        note &= 15;

        if (note > 12) {
            return;
        }

        //LOG(("%s octave:%X note:%X\n", tms.subtype, octave, note));
        /* update the stream before changing the tune */
        stream_update(tms.channel, 0);

        /* play a single note from 'tune 4', a list of the 13 tones */
        tms36xx_reset_counters(chip);
        tms.octave = octave;
        tms.tune_num = 4;
        tms.tune_ofs = note;
        tms.tune_max = note + 1;
    }

    public static void tms3617_enable_w(int chip, int enable) {
        TMS36XX tms = tms36xx[chip];
        int i, bits = 0;

        /* duplicate the 6 voice enable bits */
        enable = (enable & 0x3f) | ((enable & 0x3f) << 6);
        if (enable == tms.enable) {
            return;
        }

        /* update the stream before changing the tune */
        stream_update(tms.channel, 0);

        //LOG(("%s enable voices", tms.subtype));
        for (i = 0; i < 6; i++) {
            if ((enable & (1 << i)) != 0) {
                bits += 2;
                /* each voice has two instances */
            }
        }
        /* set the enable mask and number of active voices */
        tms.enable = enable;
        tms.voices = bits;
        //LOG(("%s\n", bits ? "" : " none"));
    }

    public tms36xx() {
        this.name = "TMS36XX";
        this.sound_num = SOUND_TMS36XX;
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((TMS36XXinterface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return 0;
    }

    @Override
    public int start(MachineSound msound) {
        int i, j;
        intf = (TMS36XXinterface) msound.sound_interface;

        for (i = 0; i < intf.num; i++) {
            int enable;
            TMS36XX tms;
            String name;

            if (intf.subtype[i] == MM6221AA) {
                name = sprintf("MM6221AA #%d", i);
            } else {
                name = sprintf("TMS36%02d #%d", intf.subtype[i], i);
            }
            tms36xx[i] = new TMS36XX();//(sizeof(struct TMS36XX));
            if (tms36xx[i] == null) {
                if (errorlog != null) {
                    fprintf(errorlog, "%s failed to malloc struct TMS36XX\n", name);
                }
                return 1;
            }
            tms = tms36xx[i];
            //memset(tms, 0, sizeof(struct TMS36XX));

            //tms.subtype = malloc(strlen(name) + 1);
            //strcpy(tms.subtype, name);
            tms.subtype = name;
            tms.channel = stream_init(name, intf.mixing_level[i], Machine.sample_rate, i, tms36xx_sound_update);

            if (tms.channel == -1) {
                if (errorlog != null) {
                    fprintf(errorlog, "%s stream_init failed\n", name);
                }
                return 1;
            }
            tms.samplerate = Machine.sample_rate != 0 ? Machine.sample_rate : 1;
            tms.basefreq = intf.basefreq[i];
            enable = 0;
            for (j = 0; j < 6; j++) {
                if (intf.decay[i][j] > 0) {
                    tms.decay[j + 0] = tms.decay[j + 6] = (int) (VMAX / intf.decay[i][j]);
                    enable |= 0x41 << j;
                }
            }
            tms.speed = (intf.speed[i] > 0) ? (int) (VMAX / intf.speed[i]) : VMAX;
            tms3617_enable_w(i, enable);

            /*LOG(("%s samplerate    %d\n", name, tms.samplerate));
			LOG(("%s basefreq      %d\n", name, tms.basefreq));
			LOG(("%s decay         %d,%d,%d,%d,%d,%d\n", name,
				tms.decay[0], tms.decay[1], tms.decay[2],
				tms.decay[3], tms.decay[4], tms.decay[5]));
	        LOG(("%s speed         %d\n", name, tms.speed));*/
        }
        return 0;
    }

    @Override
    public void stop() {
        int i;
        for (i = 0; i < intf.num; i++) {
            if (tms36xx[i] != null) {
                if (tms36xx[i].subtype != null) {
                    tms36xx[i].subtype = null;
                }
                tms36xx[i] = null;
            }
            tms36xx[i] = null;
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
