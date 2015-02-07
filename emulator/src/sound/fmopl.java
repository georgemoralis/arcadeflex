package sound;

import static sound.fmoplH.*;
import static arcadeflex.ptrlib.*;
import sound.fm_c.FM_OPL;
import static arcadeflex.libc.*;
import static arcadeflex.libc_old.*;
import static sound.YM_DELTA_T.*;
import sound.fm_c.OPL_CH;
import sound.fm_c.OPL_SLOT;

public class fmopl {

    /* -------------------- preliminary define section --------------------- */
    /* attack/decay rate time rate */
    public static final int OPL_ARRATE = 141280;  /* RATE 4 =  2826.24ms @ 3.6MHz */

    public static final int OPL_DRRATE = 1956000;  /* RATE 4 = 39280.64ms @ 3.6MHz */

    public static final int DELTAT_MIXING_LEVEL = (1); /* DELTA-T ADPCM MIXING LEVEL */

    public static final int FREQ_BITS = 24;			/* frequency turn          */

    /* counter bits = 20 , octerve 7 */
    public static final int FREQ_RATE = (1 << (FREQ_BITS - 20));
    public static final int TL_BITS = (FREQ_BITS + 2);

    public static final int OPL_OUTSB = (TL_BITS + 3 - 16);	/* OPL output final shift 16bit */

    public static final int OPL_MAXOUT = (0x7fff << OPL_OUTSB);
    public static final int OPL_MINOUT = (-0x8000 << OPL_OUTSB);

    /* -------------------- quality selection --------------------- */

    /* sinwave entries */
    /* used static memory = SIN_ENT * 4 (byte) */
    public static final int SIN_ENT = 2048;

    /* output level entries (envelope,sinwave) */
    /* envelope counter lower bits */
    public static final int ENV_BITS = 16;
    /* envelope output entries */
    public static final int EG_ENT = 4096;
    /* used dynamic memory = EG_ENT*4*4(byte)or EG_ENT*6*4(byte) */
    /* used static  memory = EG_ENT*4 (byte)                     */

    public static final int EG_OFF = ((2 * EG_ENT) << ENV_BITS);  /* OFF          */

    public static final int EG_DED = EG_OFF;
    public static final int EG_DST = (EG_ENT << ENV_BITS);      /* DECAY  START */

    public static final int EG_AED = EG_DST;
    public static final int EG_AST = 0;                       /* ATTACK START */

    public static final double EG_STEP = (96.0 / EG_ENT);/* OPL is 0.1875 dB step  */

    /* LFO table entries */
    public static final int VIB_ENT = 512;
    public static final int VIB_SHIFT = (32 - 9);
    public static final int AMS_ENT = 512;
    public static final int AMS_SHIFT = (32 - 9);

    public static final int VIB_RATE = 256;

    /* -------------------- local defines , macros --------------------- */
    /* register number to channel number , slot offset */
    public static final int SLOT1 = 0;
    public static final int SLOT2 = 1;

    /* envelope phase */
    public static final int ENV_MOD_RR = 0x00;
    public static final int ENV_MOD_DR = 0x01;
    public static final int ENV_MOD_AR = 0x02;

    /* -------------------- tables --------------------- */
    static int[] slot_array = {
        0, 2, 4, 1, 3, 5, -1, -1,
        6, 8, 10, 7, 9, 11, -1, -1,
        12, 14, 16, 13, 15, 17, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1
    };
    /* key scale level */
    public static final double ML = (0.1875 * 2 / EG_STEP);
    static int /*UINT32*/ KSL_TABLE[]
            = {
                /* OCT 0 */
                (int) (0.000 * ML), (int) (0.000 * ML), (int) (0.000 * ML), (int) (0.000 * ML),
                (int) (0.000 * ML), (int) (0.000 * ML), (int) (0.000 * ML), (int) (0.000 * ML),
                (int) (0.000 * ML), (int) (0.000 * ML), (int) (0.000 * ML), (int) (0.000 * ML),
                (int) (0.000 * ML), (int) (0.000 * ML), (int) (0.000 * ML), (int) (0.000 * ML),
                /* OCT 1 */
                (int) (0.000 * ML), (int) (0.000 * ML), (int) (0.000 * ML), (int) (0.000 * ML),
                (int) (0.000 * ML), (int) (0.000 * ML), (int) (0.000 * ML), (int) (0.000 * ML),
                (int) (0.000 * ML), (int) (0.750 * ML), (int) (1.125 * ML), (int) (1.500 * ML),
                (int) (1.875 * ML), (int) (2.250 * ML), (int) (2.625 * ML), (int) (3.000 * ML),
                /* OCT 2 */
                (int) (0.000 * ML), (int) (0.000 * ML), (int) (0.000 * ML), (int) (0.000 * ML),
                (int) (0.000 * ML), (int) (1.125 * ML), (int) (1.875 * ML), (int) (2.625 * ML),
                (int) (3.000 * ML), (int) (3.750 * ML), (int) (4.125 * ML), (int) (4.500 * ML),
                (int) (4.875 * ML), (int) (5.250 * ML), (int) (5.625 * ML), (int) (6.000 * ML),
                /* OCT 3 */
                (int) (0.000 * ML), (int) (0.000 * ML), (int) (0.000 * ML), (int) (1.875 * ML),
                (int) (3.000 * ML), (int) (4.125 * ML), (int) (4.875 * ML), (int) (5.625 * ML),
                (int) (6.000 * ML), (int) (6.750 * ML), (int) (7.125 * ML), (int) (7.500 * ML),
                (int) (7.875 * ML), (int) (8.250 * ML), (int) (8.625 * ML), (int) (9.000 * ML),
                /* OCT 4 */
                (int) (0.000 * ML), (int) (0.000 * ML), (int) (3.000 * ML), (int) (4.875 * ML),
                (int) (6.000 * ML), (int) (7.125 * ML), (int) (7.875 * ML), (int) (8.625 * ML),
                (int) (9.000 * ML), (int) (9.750 * ML), (int) (10.125 * ML), (int) (10.500 * ML),
                (int) (10.875 * ML), (int) (11.250 * ML), (int) (11.625 * ML), (int) (12.000 * ML),
                /* OCT 5 */
                (int) (0.000 * ML), (int) (3.000 * ML), (int) (6.000 * ML), (int) (7.875 * ML),
                (int) (9.000 * ML), (int) (10.125 * ML), (int) (10.875 * ML), (int) (11.625 * ML),
                (int) (12.000 * ML), (int) (12.750 * ML), (int) (13.125 * ML), (int) (13.500 * ML),
                (int) (13.875 * ML), (int) (14.250 * ML), (int) (14.625 * ML), (int) (15.000 * ML),
                /* OCT 6 */
                (int) (0.000 * ML), (int) (6.000 * ML), (int) (9.000 * ML), (int) (10.875 * ML),
                (int) (12.000 * ML), (int) (13.125 * ML), (int) (13.875 * ML), (int) (14.625 * ML),
                (int) (15.000 * ML), (int) (15.750 * ML), (int) (16.125 * ML), (int) (16.500 * ML),
                (int) (16.875 * ML), (int) (17.250 * ML), (int) (17.625 * ML), (int) (18.000 * ML),
                /* OCT 7 */
                (int) (0.000 * ML), (int) (9.000 * ML), (int) (12.000 * ML), (int) (13.875 * ML),
                (int) (15.000 * ML), (int) (16.125 * ML), (int) (16.875 * ML), (int) (17.625 * ML),
                (int) (18.000 * ML), (int) (18.750 * ML), (int) (19.125 * ML), (int) (19.500 * ML),
                (int) (19.875 * ML), (int) (20.250 * ML), (int) (20.625 * ML), (int) (21.000 * ML)
            };

    /* sustain lebel table (3db per step) */
    /* 0 - 15: 0, 3, 6, 9,12,15,18,21,24,27,30,33,36,39,42,93 (dB)*/
    static int SC(int db) {
        return (int) (db * ((3 / EG_STEP) * (1 << ENV_BITS))) + EG_DST;
    }
    static int[] SL_TABLE = {
        SC(0), SC(1), SC(2), SC(3), SC(4), SC(5), SC(6), SC(7),
        SC(8), SC(9), SC(10), SC(11), SC(12), SC(13), SC(14), SC(31)
    };

    public static final int TL_MAX = (EG_ENT * 2); /* limit(tl + ksr + envelope) + sinwave */
    /* TotalLevel : 48 24 12  6  3 1.5 0.75 (dB) */
    /* TL_TABLE[ 0      to TL_MAX          ] : plus  section */
    /* TL_TABLE[ TL_MAX to TL_MAX+TL_MAX-1 ] : minus section */

    static IntSubArray TL_TABLE;
    /* pointers to TL_TABLE with sinwave output offset */
    static IntSubArray[] SIN_TABLE;//static INT32 **SIN_TABLE;

    /* LFO table */
    static IntSubArray AMS_TABLE;
    static IntSubArray VIB_TABLE;

    /* envelope output curve table */
    /* attack + decay + OFF */
    static int[] ENV_CURVE = new int[2 * EG_ENT + 1];

    /* multiple table */
    static int[] MUL_TABLE = {
        /* 1/2, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,15 */
        1, 1 * 2, 2 * 2, 3 * 2, 4 * 2, 5 * 2, 6 * 2, 7 * 2,
        8 * 2, 9 * 2, 10 * 2, 10 * 2, 12 * 2, 12 * 2, 15 * 2, 15 * 2
    };

    /* dummy attack / decay rate ( when rate == 0 ) */
    static int[] RATE_0 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    /* -------------------- static state --------------------- */

    /* lock level of common table */
    static int num_lock = 0;

    /* work table */
    static Object cur_chip = null;	/* current chip point */
    /* currenct chip state */

    static OPL_CH[] S_CH;
    static int E_CH;//static OPL_CH E_CH;

    static OPL_SLOT SLOT7_1, SLOT7_2, SLOT8_1, SLOT8_2;

    static int[] outd = new int[1];
    static int ams;
    static int vib;
    static IntSubArray ams_table;
    static IntSubArray vib_table;
    static int amsIncr;
    static int vibIncr;
    static int[] feedback2 = new int[1];		/* connect for SLOT 2 */
   /* --------------------- subroutines  --------------------- */


    static int Limit(int val, int max, int min) {
        if (val > max) {
            val = max;
        } else if (val < min) {
            val = min;
        }

        return val;
    }
    /* status set and IRQ handling */

    static void OPL_STATUS_SET(FM_OPL OPL, int flag) {
        /* set status flag */
        /*RECHECK*/ OPL.status |= flag;
        if ((OPL.status & 0x80) == 0) {
            if ((OPL.status & OPL.statusmask) != 0) {	/* IRQ on */

                OPL.status |= 0x80;
                /* callback user interrupt handler (IRQ is OFF to ON) */
                if (OPL.IRQHandler != null) {
                    OPL.IRQHandler.handler(OPL.IRQParam, 1);
                }
            }
        }
    }
    /* status reset and IRQ handling */

    static void OPL_STATUS_RESET(FM_OPL OPL, int flag) {
        /* reset status flag */
        /*RECHECK*/ OPL.status &= ~flag;
        if ((OPL.status & 0x80) != 0) {
            if ((OPL.status & OPL.statusmask) == 0) {
                OPL.status &= 0x7f;
                /* callback user interrupt handler (IRQ is ON to OFF) */
                if (OPL.IRQHandler != null) {
                    OPL.IRQHandler.handler(OPL.IRQParam, 0);
                }
            }
        }
    }
    /* IRQ mask set */

    static void OPL_STATUSMASK_SET(FM_OPL OPL, int flag) {
        OPL.statusmask = flag;
        /* IRQ handling check */
        OPL_STATUS_SET(OPL, 0);
        OPL_STATUS_RESET(OPL, 0);
    }

    /* ----- key on  ----- */
    static void OPL_KEYON(OPL_SLOT SLOT) {
        /* sin wave restart */
        SLOT.Cnt = 0;
        /* set attack */
        SLOT.evm = ENV_MOD_AR;
        SLOT.evs = SLOT.evsa;
        SLOT.evc = EG_AST;
        SLOT.eve = EG_AED;
    }

    /* ----- key off ----- */
    static void OPL_KEYOFF(OPL_SLOT SLOT) {
        if (SLOT.evm > ENV_MOD_RR) {
            /* set envelope counter from envleope output */
            SLOT.evm = ENV_MOD_RR;
            if ((SLOT.evc & EG_DST) == 0) {
                SLOT.evc = EG_DST;//SLOT.evc = (ENV_CURVE[SLOT.evc>>ENV_BITS]<<ENV_BITS) + EG_DST;
            }
            SLOT.eve = EG_DED;
            SLOT.evs = SLOT.evsr;
        }
    }
    /* ---------- calcrate Envelope Generator & Phase Generator ---------- */
    /* return : envelope output */

    static long OPL_CALC_SLOT(OPL_SLOT SLOT) {
        /* calcrate envelope generator */
        if ((SLOT.evc += SLOT.evs) >= SLOT.eve) {
            switch (SLOT.evm) {
                case ENV_MOD_AR: /* ATTACK . DECAY1 */
                    /* next DR */

                    SLOT.evm = ENV_MOD_DR;
                    SLOT.evc = EG_DST;
                    SLOT.eve = SLOT.SL;
                    SLOT.evs = SLOT.evsd;
                    break;
                case ENV_MOD_DR: /* DECAY . SL or RR */

                    SLOT.evc = SLOT.SL;
                    SLOT.eve = EG_DED;
                    if (SLOT.eg_typ != 0) {
                        SLOT.evs = 0;
                    } else {
                        SLOT.evm = ENV_MOD_RR;
                        SLOT.evs = SLOT.evsr;
                    }
                    break;
                case ENV_MOD_RR: /* RR . OFF */

                    SLOT.evc = EG_OFF;
                    SLOT.eve = EG_OFF + 1;
                    SLOT.evs = 0;
                    break;
            }
        }
        /* calcrate envelope */
        return ((SLOT.TLL + ENV_CURVE[SLOT.evc >> ENV_BITS] + (SLOT.ams != 0 ? ams : 0)) & 0xFFFFFFFFL);
        //return SLOT->TLL+ENV_CURVE[SLOT->evc>>ENV_BITS]+(SLOT->ams ? ams : 0);
    }

    static void set_algorythm(OPL_CH CH) {
        int[] carrier = outd;
        CH.connect1 = CH.CON != 0 ? carrier : feedback2;
        CH.connect2 = carrier;
    }

    /* ---------- frequency counter for operater update ---------- */
    static void CALC_FCSLOT(OPL_CH CH, OPL_SLOT SLOT) {
        int ksr;

        /* frequency step counter */
        /*RECHECK*/ SLOT.Incr = ((CH.fc * SLOT.mul) & 0xFFFFFFFFL);
        ksr = CH.kcode >> SLOT.KSR;

        if (SLOT.ksr != (ksr &0xFF)) {
            /*RECHECK*/
            SLOT.ksr = ksr & 0xFF;
            /* attack , decay rate recalcration */
            SLOT.evsa = SLOT.AR.read(ksr);
            SLOT.evsd = SLOT.DR.read(ksr);
            SLOT.evsr = SLOT.RR.read(ksr);
        }
        SLOT.TLL = (int) (SLOT.TL + (CH.ksl_base >> SLOT.ksl));
    }

    /* set multi,am,vib,EG-TYP,KSR,mul */
    static void set_mul(FM_OPL OPL, int slot, int v) {
        OPL_CH CH = OPL.P_CH[slot / 2];
        OPL_SLOT SLOT = CH.SLOT[slot & 1];

        SLOT.mul = MUL_TABLE[v & 0x0f];
        SLOT.KSR = ((v & 0x10) != 0) ? 0 : 2;
        SLOT.eg_typ = (((v & 0x20) >> 5) & 0xFF);
        SLOT.vib = (((v & 0x40)) & 0xFF);
        SLOT.ams = ((v & 0x80) & 0xFF);
        CALC_FCSLOT(CH, SLOT);
    }
    /* set ksl & tl */

    static void set_ksl_tl(FM_OPL OPL, int slot, int v) {
        OPL_CH CH = OPL.P_CH[slot / 2];
        OPL_SLOT SLOT = CH.SLOT[slot & 1];
        int ksl = v >> 6; /* 0 / 1.5 / 3 / 6 db/OCT */

        /*RECHECK*/
        SLOT.ksl = (ksl != 0 ? 3 - ksl : 31);
        SLOT.TL = (int) ((v & 0x3f) * (0.75 / EG_STEP)); /* 0.75db step */

        if ((OPL.mode & 0x80) == 0) {	/* not CSM latch total level */

            SLOT.TLL = (int) (SLOT.TL + (CH.ksl_base >> SLOT.ksl));
        }
    }
    /* set attack rate & decay rate  */

    static void set_ar_dr(FM_OPL OPL, int slot, int v) {
        OPL_CH CH = OPL.P_CH[slot / 2];
        OPL_SLOT SLOT = CH.SLOT[slot & 1];
        int ar = v >> 4;
        int dr = v & 0x0f;

        SLOT.AR = ar != 0 ? new IntSubArray(OPL.AR_TABLE, ar << 2) : new IntSubArray(RATE_0);
        SLOT.evsa = SLOT.AR.read(SLOT.ksr);
        if (SLOT.evm == ENV_MOD_AR) {
            SLOT.evs = SLOT.evsa;
        }

        SLOT.DR = dr != 0 ? new IntSubArray(OPL.DR_TABLE, dr << 2) : new IntSubArray(RATE_0);
        SLOT.evsd = SLOT.DR.read(SLOT.ksr);
        if (SLOT.evm == ENV_MOD_DR) {
            SLOT.evs = SLOT.evsd;
        }

    }

    /* set sustain level & release rate */
    static void set_sl_rr(FM_OPL OPL, int slot, int v) {
        OPL_CH CH = OPL.P_CH[slot / 2];
        OPL_SLOT SLOT = CH.SLOT[slot & 1];
        int sl = v >> 4;
        int rr = v & 0x0f;

        SLOT.SL = SL_TABLE[sl];
        if (SLOT.evm == ENV_MOD_DR) {
            SLOT.eve = SLOT.SL;
        }
        SLOT.RR = new IntSubArray(OPL.DR_TABLE, rr << 2);
        SLOT.evsr = SLOT.RR.read(SLOT.ksr);
        if (SLOT.evm == ENV_MOD_RR) {
            SLOT.evs = SLOT.evsr;
        }
    }

    //* operator output calcrator */
    //#define OP_OUT(slot,env,con)   slot->wavetable[((slot->Cnt+con)/(0x1000000/SIN_ENT))&(SIN_ENT-1)][env]

    static int OP_OUT(OPL_SLOT slot, int env, int con) {

        return slot.wavetable[(int) (((slot.Cnt + con) / (0x1000000 / SIN_ENT)) & (SIN_ENT - 1))+slot.wt_offset].read(env);
    }
    /* ---------- calcrate one of channel ---------- */

    static void OPL_CALC_CH(OPL_CH CH) {
        long env_out;
        OPL_SLOT SLOT;

        feedback2[0] = 0;
        /* SLOT 1 */
        SLOT = CH.SLOT[SLOT1];
        env_out = OPL_CALC_SLOT(SLOT);
        if (env_out < EG_ENT - 1) {
            /* PG */
            if (SLOT.vib != 0) {
                SLOT.Cnt = (SLOT.Cnt + (SLOT.Incr * vib / VIB_RATE)) & 0xFFFFFFFFL;
            } else {
                SLOT.Cnt += SLOT.Incr;
            }
            /* connectoion */
            if (CH.FB != 0) {
                int feedback1 = (CH.op1_out[0] + CH.op1_out[1]) >> CH.FB;
                CH.op1_out[1] = CH.op1_out[0];
                CH.connect1[0] += CH.op1_out[0] = OP_OUT(SLOT, (int) env_out, feedback1);
            } else {
                CH.connect1[0] += OP_OUT(SLOT, (int) env_out, 0);
            }
        } else {
            CH.op1_out[1] = CH.op1_out[0];
            CH.op1_out[0] = 0;
        }
        /* SLOT 2 */
        SLOT = CH.SLOT[SLOT2];
        env_out = OPL_CALC_SLOT(SLOT);
        if (env_out < EG_ENT - 1) {
            /* PG */
            if (SLOT.vib != 0) {
                SLOT.Cnt = (SLOT.Cnt + (SLOT.Incr * vib / VIB_RATE)) & 0xFFFFFFFFL;
            } else {
                SLOT.Cnt += SLOT.Incr;
            }
            /* connectoion */
            outd[0] += OP_OUT(SLOT, (int) env_out, feedback2[0]);
        }
    }
    /* ---------- calcrate rythm block ---------- */
    public static final double WHITE_NOISE_db = 6.0;

    static void OPL_CALC_RH(OPL_CH[] CH) {
        long env_tam, env_sd, env_top, env_hh;
        int whitenoise = (int) ((rand() & 1) * (WHITE_NOISE_db / EG_STEP));
        int tone8;

        OPL_SLOT SLOT;
        int env_out;
        /* BD : same as FM serial mode and output level is large */
        feedback2[0] = 0;
        /* SLOT 1 */
        SLOT = CH[6].SLOT[SLOT1];
        env_out = (int) OPL_CALC_SLOT(SLOT);
        if (env_out < EG_ENT - 1) {
            /* PG */
            if (SLOT.vib != 0) {
                SLOT.Cnt = (SLOT.Cnt + ((SLOT.Incr * vib / VIB_RATE))) & 0xFFFFFFFFL;
            } else {
                SLOT.Cnt += SLOT.Incr;
            }
            /* connectoion */
            if (CH[6].FB != 0) {
                int feedback1 = (CH[6].op1_out[0] + CH[6].op1_out[1]) >> CH[6].FB;
                CH[6].op1_out[1] = CH[6].op1_out[0];
                feedback2[0] = CH[6].op1_out[0] = OP_OUT(SLOT, env_out, feedback1);
            } else {
                feedback2[0] = OP_OUT(SLOT, env_out, 0);
            }
        } else {
            feedback2[0] = 0;
            CH[6].op1_out[1] = CH[6].op1_out[0];
            CH[6].op1_out[0] = 0;
        }
        /* SLOT 2 */
        SLOT = CH[6].SLOT[SLOT2];
        env_out = (int) OPL_CALC_SLOT(SLOT);
        if (env_out < EG_ENT - 1) {
            /* PG */
            if (SLOT.vib != 0) {
                SLOT.Cnt = (SLOT.Cnt + ((SLOT.Incr * vib / VIB_RATE))) & 0xFFFFFFFFL;
            } else {
                SLOT.Cnt += SLOT.Incr;
            }
            /* connectoion */
            outd[0] += OP_OUT(SLOT, env_out, feedback2[0]) * 2;
        }
// SD  (17) = mul14[fnum7] + white noise
        // TAM (15) = mul15[fnum8]
        // TOP (18) = fnum6(mul18[fnum8]+whitenoise)
        // HH  (14) = fnum7(mul18[fnum8]+whitenoise) + white noise
        env_sd = (OPL_CALC_SLOT(SLOT7_2) + whitenoise) & 0xFFFFFFFFL;
        env_tam = OPL_CALC_SLOT(SLOT8_1);
        env_top = OPL_CALC_SLOT(SLOT8_2);
        env_hh = (OPL_CALC_SLOT(SLOT7_1) + whitenoise) & 0xFFFFFFFFL;

        /* PG */
        if (SLOT7_1.vib != 0) {
            SLOT7_1.Cnt = (SLOT7_1.Cnt + (2 * SLOT7_1.Incr * vib / VIB_RATE)) & 0xFFFFFFFFL;
        } else {
            SLOT7_1.Cnt += 2 * SLOT7_1.Incr;
        }
        if (SLOT7_2.vib != 0) {
            SLOT7_2.Cnt = (SLOT7_2.Cnt + ((CH[7].fc * 8) * vib / VIB_RATE)) & 0xFFFFFFFFL;
        } else {
            SLOT7_2.Cnt += (CH[7].fc * 8);
        }
        if (SLOT8_1.vib != 0) {
            SLOT8_1.Cnt = (SLOT8_1.Cnt + (SLOT8_1.Incr * vib / VIB_RATE)) & 0xFFFFFFFFL;
        } else {
            SLOT8_1.Cnt += SLOT8_1.Incr;
        }
        if (SLOT8_2.vib != 0) {
            SLOT8_2.Cnt = (SLOT8_2.Cnt + ((CH[8].fc * 48) * vib / VIB_RATE)) & 0xFFFFFFFFL;
        } else {
            SLOT8_2.Cnt = (SLOT8_2.Cnt + (CH[8].fc * 48)) & 0xFFFFFFFFL;
        }

        tone8 = OP_OUT(SLOT8_2, whitenoise, 0);

        /* SD */
        if (env_sd < EG_ENT - 1) {
            outd[0] += OP_OUT(SLOT7_1, (int) env_sd, 0) * 8;
        }
        /* TAM */
        if (env_tam < EG_ENT - 1) {
            outd[0] += OP_OUT(SLOT8_1, (int) env_tam, 0) * 2;
        }
        /* TOP-CY */
        if (env_top < EG_ENT - 1) {
            outd[0] += OP_OUT(SLOT7_2, (int) env_top, tone8) * 2;
        }
        /* HH */
        if (env_hh < EG_ENT - 1) {
            outd[0] += OP_OUT(SLOT7_2, (int) env_hh, tone8) * 2;
        }
    }


    /* ----------- initialize time tabls ----------- */
    static void init_timetables(FM_OPL OPL, int ARRATE, int DRRATE) {
        int i;
        double rate;

        /* make attack rate & decay rate tables */
        for (i = 0; i < 4; i++) {
            OPL.AR_TABLE[i] = OPL.DR_TABLE[i] = 0;
        }
        for (i = 4; i <= 60; i++) {
            rate = OPL.freqbase;						/* frequency rate */

            if (i < 60) {
                rate *= 1.0 + (i & 3) * 0.25;		/* b0-1 : x1 , x1.25 , x1.5 , x1.75 */

            }
            rate *= 1 << ((i >> 2) - 1);						/* b2-5 : shift bit */

            rate *= (double) (EG_ENT << ENV_BITS);
            OPL.AR_TABLE[i] = (int) (rate / ARRATE);
            OPL.DR_TABLE[i] = (int) (rate / DRRATE);
        }
        for (i = 60; i < 75; i++) {
            OPL.AR_TABLE[i] = EG_AED - 1;
            OPL.DR_TABLE[i] = OPL.DR_TABLE[60];
        }
    }

    /* ---------- generic table initialize ---------- */
    static int OPLOpenTable() {
        int s, t;
        double rate;
        int i, j;
        double pom;

        /* allocate dynamic tables */
        TL_TABLE = new IntSubArray(TL_MAX * 2);
        SIN_TABLE = new IntSubArray[SIN_ENT * 4];
        AMS_TABLE = new IntSubArray(AMS_ENT * 2);
        VIB_TABLE = new IntSubArray(VIB_ENT * 2);
        /* make total level table */
        for (t = 0; t < EG_ENT - 1; t++) {
            rate = ((1 << TL_BITS) - 1) / Math.pow(10, EG_STEP * t / 20);	/* dB . voltage */

            TL_TABLE.write(t, (int) rate);
            TL_TABLE.write(TL_MAX + t, -TL_TABLE.read(t));
            /*Log(LOG_INF,"TotalLevel(%3d) = %x\n",t,TL_TABLE[t]);*/
        }
        /* fill volume off area */
        for (t = EG_ENT - 1; t < TL_MAX; t++) {

            TL_TABLE.write(t, 0);
            TL_TABLE.write(TL_MAX + t, 0);//TL_TABLE[t] = TL_TABLE[TL_MAX + t] = 0;
        }

        /* make sinwave table (total level offet) */
        /* degree 0 = degree 180                   = off */
        SIN_TABLE[0] = SIN_TABLE[SIN_ENT / 2] = new IntSubArray(TL_TABLE, EG_ENT - 1);
        for (s = 1; s <= SIN_ENT / 4; s++) {
            pom = Math.sin(2 * Math.PI * s / SIN_ENT); /* sin     */

            pom = 20 * Math.log10(1 / pom);	   /* decibel */

            j = (int) (pom / EG_STEP);         /* TL_TABLE steps */

            /* degree 0   -  90    , degree 180 -  90 : plus section */
            SIN_TABLE[s] = SIN_TABLE[SIN_ENT / 2 - s] = new IntSubArray(TL_TABLE, j);
            /* degree 180 - 270    , degree 360 - 270 : minus section */
            SIN_TABLE[SIN_ENT / 2 + s] = SIN_TABLE[SIN_ENT - s] = new IntSubArray(TL_TABLE, TL_MAX + j);
            /*		Log(LOG_INF,"sin(%3d) = %f:%f db\n",s,pom,(double)j * EG_STEP);*/
        }
        for (s = 0; s < SIN_ENT; s++) {
            SIN_TABLE[SIN_ENT * 1 + s] = s < (SIN_ENT / 2) ? SIN_TABLE[s] : new IntSubArray(TL_TABLE, EG_ENT);
            SIN_TABLE[SIN_ENT * 2 + s] = SIN_TABLE[s % (SIN_ENT / 2)];
            SIN_TABLE[SIN_ENT * 3 + s] = ((s / (SIN_ENT / 4)) & 1) != 0 ? new IntSubArray(TL_TABLE, EG_ENT) : SIN_TABLE[SIN_ENT * 2 + s];
        }
        /* envelope counter . envelope output table */
        for (i = 0; i < EG_ENT; i++) {
            /* ATTACK curve */
            pom = Math.pow(((double) (EG_ENT - 1 - i) / EG_ENT), 8) * EG_ENT;
            /* if( pom >= EG_ENT ) pom = EG_ENT-1; */
            ENV_CURVE[i] = (int) pom;
            /* DECAY ,RELEASE curve */
            ENV_CURVE[(EG_DST >> ENV_BITS) + i] = i;
        }
        /* off */
        ENV_CURVE[EG_OFF >> ENV_BITS] = EG_ENT - 1;
        /* make LFO ams table */
        for (i = 0; i < AMS_ENT; i++) {
            pom = (1.0 + Math.sin(2 * Math.PI * i / AMS_ENT)) / 2; /* sin */

            AMS_TABLE.write(i, (int) ((1.0 / EG_STEP) * pom)); /* 1dB   */

            AMS_TABLE.write(AMS_ENT + i, (int) ((4.8 / EG_STEP) * pom)); /* 4.8dB */

        }
        /* make LFO vibrate table */
        for (i = 0; i < VIB_ENT; i++) {
            /* 100cent = 1seminote = 6% ?? */
            pom = (double) VIB_RATE * 0.06 * Math.sin(2 * Math.PI * i / VIB_ENT); /* +-100sect step */

            VIB_TABLE.write(i, (int) (VIB_RATE + (pom * 0.07))); /* +- 7cent */

            VIB_TABLE.write(VIB_ENT + i, (int) (VIB_RATE + (pom * 0.14))); /* +-14cent */
            /* Log(LOG_INF,"vib %d=%d\n",i,VIB_TABLE[VIB_ENT+i]); */

        }
        return 1;
    }

    static void OPLCloseTable() {
        TL_TABLE = null;
        SIN_TABLE = null;
        AMS_TABLE = null;
        VIB_TABLE = null;
    }

    /* CSM Key Controll */
    static void CSMKeyControll(OPL_CH CH) {
        OPL_SLOT slot1 = CH.SLOT[SLOT1];
        OPL_SLOT slot2 = CH.SLOT[SLOT2];
        /* all key off */
        OPL_KEYOFF(slot1);
        OPL_KEYOFF(slot2);
        /* total level latch */
        slot1.TLL = (int) (slot1.TL + (CH.ksl_base >> slot1.ksl));
        slot1.TLL = (int) (slot1.TL + (CH.ksl_base >> slot1.ksl));
        /* key on */
        CH.op1_out[0] = CH.op1_out[1] = 0;
        OPL_KEYON(slot1);
        OPL_KEYON(slot2);
    }
    /* ---------- opl initialize ---------- */

    public static void OPL_initalize(FM_OPL OPL) {
        int fn;

        /* frequency base */
        OPL.freqbase = (OPL.rate) != 0 ? ((double) OPL.clock / OPL.rate) / 72 : 0;

        /* Timer base time */
        OPL.TimerBase = 1.0 / ((double) OPL.clock / 72.0);
        /* make time tables */
        init_timetables(OPL, OPL_ARRATE, OPL_DRRATE);
        /* make fnumber -> increment counter table */
        for (fn = 0; fn < 1024; fn++) {
            /*RECHECK*/ OPL.FN_TABLE[fn] = (long) (OPL.freqbase * fn * FREQ_RATE * (1 << 7) / 2) & 0xffffffffL;//converting to unsigned
        }
        /* LFO freq.table */
        OPL.amsIncr = (int) (OPL.rate != 0 ? (double) AMS_ENT * (1 << AMS_SHIFT) / OPL.rate * 3.7 * ((double) OPL.clock / 3600000) : 0);
        OPL.vibIncr = (int) (OPL.rate != 0 ? (double) VIB_ENT * (1 << VIB_SHIFT) / OPL.rate * 6.4 * ((double) OPL.clock / 3600000) : 0);

    }

    /* ---------- write a OPL registers ---------- */
    public static void OPLWriteReg(FM_OPL OPL, int r, int v) {

        OPL_CH CH;
        int slot;
        int block_fnum;
        switch (r & 0xe0) {
            case 0x00: /* 00-1f:controll */

                switch (r & 0x1f) {

                    case 0x01:
                        /* wave selector enable */
                        if ((OPL.type & OPL_TYPE_WAVESEL) != 0) {
                            /*RECHECK*/
                            OPL.wavesel = ((v & 0x20) & 0xFF);
                            if (OPL.wavesel == 0) {
                                /* preset compatible mode */
                                int c;
                                for (c = 0; c < OPL.max_ch; c++) {
                                    OPL.P_CH[c].SLOT[SLOT1].wt_offset = 0;
                                    OPL.P_CH[c].SLOT[SLOT1].wavetable = SIN_TABLE;//OPL->P_CH[c].SLOT[SLOT1].wavetable = &SIN_TABLE[0];
                                    OPL.P_CH[c].SLOT[SLOT2].wavetable = SIN_TABLE;//OPL->P_CH[c].SLOT[SLOT2].wavetable = &SIN_TABLE[0];
                                }
                            }
                        }
                        return;
                    case 0x02:	/* Timer 1 */

                        OPL.T[0] = (256 - v) * 4;
                        break;
                    case 0x03:	/* Timer 2 */

                        OPL.T[1] = (256 - v) * 16;
                        return;
                    case 0x04:	/* IRQ clear / mask and Timer enable */

                        if ((v & 0x80) != 0) {	/* IRQ flag clear */

                            OPL_STATUS_RESET(OPL, 0x7f);
                        } else {	/* set IRQ mask ,timer enable*/
                            /*RECHECK*/

                            int/*UINT8*/ st1 = ((v & 1) & 0xFF);
                            /*RECHECK*/
                            int/*UINT8*/ st2 = (((v >> 1) & 1) & 0xFF);

                            /* IRQRST,T1MSK,t2MSK,EOSMSK,BRMSK,x,ST2,ST1 */
                            OPL_STATUS_RESET(OPL, v & 0x78);
                            OPL_STATUSMASK_SET(OPL, ((~v) & 0x78) | 0x01);
                            /* timer 2 */
                            if (OPL.st[1] != st2) {
                                double interval = st2 != 0 ? (double) OPL.T[1] * OPL.TimerBase : 0.0;
                                OPL.st[1] = st2;
                                if (OPL.TimerHandler != null) {
                                    OPL.TimerHandler.handler(OPL.TimerParam + 1, interval);
                                }
                            }
                            /* timer 1 */
                            if (OPL.st[0] != st1) {
                                double interval = st1 != 0 ? (double) OPL.T[0] * OPL.TimerBase : 0.0;
                                OPL.st[0] = st1;
                                if (OPL.TimerHandler != null) {
                                    OPL.TimerHandler.handler(OPL.TimerParam + 0, interval);
                                }
                            }
                        }
                        return;
		case 0x06:		/* Key Board OUT */
			if((OPL.type&OPL_TYPE_KEYBOARD)!=0)
			{
				if(OPL.keyboardhandler_w!=null)
					OPL.keyboardhandler_w.handler(OPL.keyboard_param,v);
				else
                                {
					//Log(LOG_WAR,"OPL:write unmapped KEYBOARD port\n");
                                }
			}
			return;
		case 0x07:	/* DELTA-T controll : START,REC,MEMDATA,REPT,SPOFF,x,x,RST */
			if((OPL.type&OPL_TYPE_ADPCM)!=0)
				YM_DELTAT_ADPCM_Write(OPL.deltat,r-0x07,v);
			return;
		case 0x08:	/* MODE,DELTA-T : CSM,NOTESEL,x,x,smpl,da/ad,64k,rom */
			OPL.mode = v;
			v&=0x1f;	/* for DELTA-T unit */
		case 0x09:		/* START ADD */
		case 0x0a:
		case 0x0b:		/* STOP ADD  */
		case 0x0c:
		case 0x0d:		/* PRESCALE   */
		case 0x0e:
		case 0x0f:		/* ADPCM data */
		case 0x10: 		/* DELTA-N    */
		case 0x11: 		/* DELTA-N    */
		case 0x12: 		/* EG-CTRL    */
			if((OPL.type&OPL_TYPE_ADPCM)!=0)
				YM_DELTAT_ADPCM_Write(OPL.deltat,r-0x07,v);
			return;
                }
                break;
            case 0x20:	/* am,vib,ksr,eg type,mul */

                slot = slot_array[r & 0x1f];
                if (slot == -1) {
                    return;
                }
                set_mul(OPL, slot, v);
                return;
            case 0x40:
                slot = slot_array[r & 0x1f];
                if (slot == -1) {
                    return;
                }
                set_ksl_tl(OPL, slot, v);
                return;
            case 0x60:
                slot = slot_array[r & 0x1f];
                if (slot == -1) {
                    return;
                }
                set_ar_dr(OPL, slot, v);
                return;
            case 0x80:
                slot = slot_array[r & 0x1f];
                if (slot == -1) {
                    return;
                }
                set_sl_rr(OPL, slot, v);
                return;
            case 0xa0:
                switch (r) {
                    case 0xbd: /* amsep,vibdep,r,bd,sd,tom,tc,hh */ {
                        int rkey = ((OPL.rythm ^ v) & 0xFF);
                        OPL.ams_table = new IntSubArray(AMS_TABLE, (v & 0x80) != 0 ? AMS_ENT : 0);
                        OPL.vib_table = new IntSubArray(VIB_TABLE, (v & 0x40) != 0 ? VIB_ENT : 0);
                        OPL.rythm = ((v & 0x3f) & 0xFF);

                        if ((OPL.rythm & 0x20) != 0) {
                            /* BD key on/off */
                            if ((rkey & 0x10) != 0) {
                                if ((v & 0x10) != 0) {
                                    OPL.P_CH[6].op1_out[0] = OPL.P_CH[6].op1_out[1] = 0;
                                    OPL_KEYON(OPL.P_CH[6].SLOT[SLOT1]);
                                    OPL_KEYON(OPL.P_CH[6].SLOT[SLOT2]);
                                } else {
                                    OPL_KEYOFF(OPL.P_CH[6].SLOT[SLOT1]);
                                    OPL_KEYOFF(OPL.P_CH[6].SLOT[SLOT2]);
                                }
                            }
                            /* SD key on/off */
                            if ((rkey & 0x08) != 0) {
                                if ((v & 0x08) != 0) {
                                    OPL_KEYON(OPL.P_CH[7].SLOT[SLOT2]);
                                } else {
                                    OPL_KEYOFF(OPL.P_CH[7].SLOT[SLOT2]);
                                }
                            }/* TAM key on/off */

                            if ((rkey & 0x04) != 0) {
                                if ((v & 0x04) != 0) {
                                    OPL_KEYON(OPL.P_CH[8].SLOT[SLOT1]);
                                } else {
                                    OPL_KEYOFF(OPL.P_CH[8].SLOT[SLOT1]);
                                }
                            }
                            /* TOP-CY key on/off */
                            if ((rkey & 0x02) != 0) {
                                if ((v & 0x02) != 0) {
                                    OPL_KEYON(OPL.P_CH[8].SLOT[SLOT2]);
                                } else {
                                    OPL_KEYOFF(OPL.P_CH[8].SLOT[SLOT2]);
                                }
                            }
                            /* HH key on/off */
                            if ((rkey & 0x01) != 0) {
                                if ((v & 0x01) != 0) {
                                    OPL_KEYON(OPL.P_CH[7].SLOT[SLOT1]);
                                } else {
                                    OPL_KEYOFF(OPL.P_CH[7].SLOT[SLOT1]);
                                }
                            }
                        }
                    }
                    return;
                }
                /* keyon,block,fnum */
                if ((r & 0x0f) > 8) {
                    return;
                }
                CH = OPL.P_CH[r & 0x0f];
                if ((r & 0x10) == 0) {	/* a0-a8 */

                    block_fnum = (int) (CH.block_fnum & 0x1f00) | v;
                } else {	/* b0-b8 */

                    int keyon = (v >> 5) & 1;
                    block_fnum = (int) (((v & 0x1f) << 8) | (CH.block_fnum & 0xff));
                    if (CH.keyon != keyon) {
                        if ((CH.keyon = keyon) != 0) {
                            CH.op1_out[0] = CH.op1_out[1] = 0;
                            OPL_KEYON(CH.SLOT[SLOT1]);
                            OPL_KEYON(CH.SLOT[SLOT2]);
                        } else {
                            OPL_KEYOFF(CH.SLOT[SLOT1]);
                            OPL_KEYOFF(CH.SLOT[SLOT2]);
                        }
                    }
                }
                /* update */
                if (CH.block_fnum != block_fnum) {
                    int blockRv = 7 - (block_fnum >> 10);
                    int fnum = block_fnum & 0x3ff;
                    CH.block_fnum = block_fnum & 0xFFFFFFFFL;

                    CH.ksl_base = KSL_TABLE[block_fnum >> 6];
                    CH.fc = OPL.FN_TABLE[fnum] >> blockRv;
                    CH.kcode = (int) ((CH.block_fnum >> 9) & 0xFF);
                    if ((OPL.mode & 0x40) != 0 && (CH.block_fnum & 0x100) != 0) {
                        CH.kcode |= 1;
                    }
                    CALC_FCSLOT(CH, CH.SLOT[SLOT1]);
                    CALC_FCSLOT(CH, CH.SLOT[SLOT2]);
                }
                return;
            case 0xc0:
                /* FB,C */
                if ((r & 0x0f) > 8) {
                    return;
                }
                CH = OPL.P_CH[r & 0x0f];
                 {
                    int feedback = (v >> 1) & 7;
                    CH.FB = ((feedback != 0 ? (8 + 1) - feedback : 0) & 0xFF);
                    CH.CON = ((v & 1) & 0xFF);
                    set_algorythm(CH);
                }
                return;
            case 0xe0: /* wave type */

                slot = slot_array[r & 0x1f];
                if (slot == -1) {
                    return;
                }
                CH = OPL.P_CH[slot / 2];
                if (OPL.wavesel != 0) {
                    /* Log(LOG_INF,"OPL SLOT %d wave select %d\n",slot,v&3); */
                    CH.SLOT[slot & 1].wt_offset=(v & 0x03) * SIN_ENT;//CH.SLOT[slot & 1].wavetable = new IntSubArray(SIN_TABLE[(v & 0x03) * SIN_ENT]);
                }
                return;
            default:
                System.out.println("case =" + (r & 0xe0) + " r=" + r + " v=" + v);
                break;
        }
    }

    //* lock/unlock for common table */
    static int OPL_LockTable() {
        num_lock++;
        if (num_lock > 1) {
            return 0;
        }
        /* first time */
        cur_chip = null;
        /* allocate total level table (128kb space) */
        if (OPLOpenTable() == 0) {
            num_lock--;
            return -1;
        }
        return 0;
    }

    static void OPL_UnLockTable() {
        if (num_lock != 0) {
            num_lock--;
        }
        if (num_lock != 0) {
            return;
        }
        /* last time */
        cur_chip = null;
        OPLCloseTable();
    }

   /*******************************************************************************/
   /*		YM3812 local section                                                   */
   /*******************************************************************************/
   
   /* ---------- update one of chip ----------- */
    public static void YM3812UpdateOne(FM_OPL OPL, UShortPtr buffer, int length) {
        int i;
        int data;
        UShortPtr buf = buffer;
        long amsCnt = OPL.amsCnt;
        long vibCnt = OPL.vibCnt;
        int rythm = ((OPL.rythm & 0x20) & 0xFF);
        OPL_CH CH;
        int R_CH;

        if ((Object) OPL != cur_chip) {
            cur_chip = OPL;
            /* channel pointers */
            S_CH = OPL.P_CH;
            E_CH = 9;// S_CH[9];
                /* rythm slot */
            SLOT7_1 = S_CH[7].SLOT[SLOT1];
            SLOT7_2 = S_CH[7].SLOT[SLOT2];
            SLOT8_1 = S_CH[8].SLOT[SLOT1];
            SLOT8_2 = S_CH[8].SLOT[SLOT2];
            /* LFO state */
            amsIncr = OPL.amsIncr;
            vibIncr = OPL.vibIncr;
            ams_table = OPL.ams_table;
            vib_table = OPL.vib_table;
        }
        R_CH = rythm != 0 ? 6 : E_CH;
        for (i = 0; i < length; i++) {
            /*            channel A         channel B         channel C      */
            /* LFO */
            ams = ams_table.read((int) ((amsCnt = (amsCnt + amsIncr) & 0xFFFFFFFFL) >> AMS_SHIFT));//recheck
            vib = vib_table.read((int) ((vibCnt = (vibCnt + vibIncr) & 0xFFFFFFFFL) >> VIB_SHIFT));//recheck
            outd[0] = 0;

            /* FM part */
            for (int k = 0; k != R_CH; k++) {
                CH = S_CH[k];
                OPL_CALC_CH(CH);
            }
            /* Rythn part */
            if (rythm != 0) {
                OPL_CALC_RH(S_CH);
            }
            /* limit check */
            data = Limit(outd[0], OPL_MAXOUT, OPL_MINOUT);
            /* store to sound buffer */
            buf.write(i, (char) (data >> OPL_OUTSB));
        }
        OPL.amsCnt = (int) amsCnt;
        OPL.vibCnt = (int) vibCnt;
    }
    public static void Y8950UpdateOne(FM_OPL OPL, UShortPtr buffer, int length) {
        int i;
        int data;
        UShortPtr buf = buffer;
        long amsCnt = OPL.amsCnt;
        long vibCnt = OPL.vibCnt;
        int rythm = ((OPL.rythm & 0x20) & 0xFF);
        OPL_CH CH;
        int R_CH;
	YM_DELTAT DELTAT = OPL.deltat;

        /* setup DELTA-T unit */
 	YM_DELTAT_DECODE_PRESET(DELTAT);

        if ((Object) OPL != cur_chip) {
            cur_chip = OPL;
            /* channel pointers */
            S_CH = OPL.P_CH;
            E_CH = 9;// S_CH[9];
                /* rythm slot */
            SLOT7_1 = S_CH[7].SLOT[SLOT1];
            SLOT7_2 = S_CH[7].SLOT[SLOT2];
            SLOT8_1 = S_CH[8].SLOT[SLOT1];
            SLOT8_2 = S_CH[8].SLOT[SLOT2];
            /* LFO state */
            amsIncr = OPL.amsIncr;
            vibIncr = OPL.vibIncr;
            ams_table = OPL.ams_table;
            vib_table = OPL.vib_table;
    	}
        R_CH = rythm != 0 ? 6 : E_CH;
        for (i = 0; i < length; i++) {
            /*            channel A         channel B         channel C      */
            /* LFO */
            ams = ams_table.read((int) ((amsCnt = (amsCnt + amsIncr) & 0xFFFFFFFFL) >> AMS_SHIFT));//recheck
            vib = vib_table.read((int) ((vibCnt = (vibCnt + vibIncr) & 0xFFFFFFFFL) >> VIB_SHIFT));//recheck
            outd[0] = 0;
   		/* deltaT ADPCM */
   	    if( DELTAT.flag!=0 )
   		YM_DELTAT_ADPCM_CALC(DELTAT);
            /* FM part */
            for (int k = 0; k != R_CH; k++) {
                CH = S_CH[k];
                OPL_CALC_CH(CH);
            }
            /* Rythn part */
            if (rythm != 0) {
                OPL_CALC_RH(S_CH);
            }
            /* limit check */
            data = Limit(outd[0], OPL_MAXOUT, OPL_MINOUT);
            /* store to sound buffer */
            buf.write(i, (char) (data >> OPL_OUTSB));
        }
        OPL.amsCnt = (int) amsCnt;
        OPL.vibCnt = (int) vibCnt;
 	/* deltaT START flag */
	if( DELTAT.flag==0 )
		OPL.status &= 0xfe;
    }

    /* ---------- reset one of chip ---------- */
    public static void OPLResetChip(FM_OPL OPL) {
        int c, s;
        int i;
        /* reset chip */
        OPL.mode = 0;	/* normal mode */

        OPL_STATUS_RESET(OPL, 0x7f);
        /* reset with register write */
        OPLWriteReg(OPL, 0x01, 0); /* wabesel disable */

        OPLWriteReg(OPL, 0x02, 0); /* Timer1 */

        OPLWriteReg(OPL, 0x03, 0); /* Timer2 */

        OPLWriteReg(OPL, 0x04, 0); /* IRQ mask clear */

        for (i = 0xff; i >= 0x20; i--) {
            OPLWriteReg(OPL, i, 0);
        }
        /* reset OPerator paramater */
        for (c = 0; c < OPL.max_ch; c++) {
            OPL_CH CH = OPL.P_CH[c];
            /* OPL.P_CH[c].PAN = OPN_CENTER; */
            for (s = 0; s < 2; s++) {
                /* wave table */
                CH.SLOT[s].wt_offset = 0;
                CH.SLOT[s].wavetable = SIN_TABLE;//CH->SLOT[s].wavetable = &SIN_TABLE[0];
                    /* CH.SLOT[s].evm = ENV_MOD_RR; */
                CH.SLOT[s].evc = EG_OFF;
                CH.SLOT[s].eve = EG_OFF + 1;
                CH.SLOT[s].evs = 0;
            }
        }
        if ((OPL.type & OPL_TYPE_ADPCM) != 0)
            {
                YM_DELTAT DELTAT = OPL.deltat;
                DELTAT.freqbase = OPL.freqbase;
                DELTAT.output_pointer = outd;
                DELTAT.portshift = 5;
                DELTAT.output_range = DELTAT_MIXING_LEVEL << TL_BITS;
                YM_DELTAT_ADPCM_Reset(DELTAT, 0);
            }
    }

    /* ----------  Create one of vietual YM3812 ----------       */
    /* 'rate'  is sampling rate and 'bufsiz' is the size of the  */
    public static FM_OPL OPLCreate(int type, int clock, int rate) {
        FM_OPL OPL;
        int max_ch = 9; /* normaly 9 channels */

        if (OPL_LockTable() == -1) {
            return null;
        }

        OPL = new FM_OPL();  //OPL        = (FM_OPL *)ptr; ptr+=sizeof(FM_OPL);

        OPL.P_CH = new OPL_CH[max_ch];//	OPL->P_CH  = (OPL_CH *)ptr; ptr+=sizeof(OPL_CH)*max_ch;
        for (int i = 0; i < max_ch; i++) {
            OPL.P_CH[i] = new OPL_CH();
        }
        if ((type & OPL_TYPE_ADPCM) != 0)
        {
                OPL.deltat = new YM_DELTAT();
        }
   	/* set channel state pointer */
        OPL.type = type;
        OPL.clock = clock;
        OPL.rate = rate;
        OPL.max_ch = max_ch;
        /* init grobal tables */
        OPL_initalize(OPL);
        /* reset chip */
        OPLResetChip(OPL);
        return OPL;
    }
    /* ----------  Destroy one of vietual YM3812 ----------       */

    public static void OPLDestroy(FM_OPL OPL) {
        OPL_UnLockTable();
   	OPL=null;
    }
    /* ----------  Option handlers ----------       */

    public static void OPLSetTimerHandler(FM_OPL OPL, OPL_TIMERHANDLERPtr TimerHandler, int channelOffset) {
        OPL.TimerHandler = TimerHandler;
        OPL.TimerParam = channelOffset;
    }

    public static void OPLSetIRQHandler(FM_OPL OPL, OPL_IRQHANDLERPtr IRQHandler, int param) {
        OPL.IRQHandler = IRQHandler;
        OPL.IRQParam = param;
    }

    public static void OPLSetUpdateHandler(FM_OPL OPL, OPL_UPDATEHANDLERPtr UpdateHandler, int param) {
        OPL.UpdateHandler = UpdateHandler;
        OPL.UpdateParam = param;
    }
   
   public static void OPLSetPortHandler(FM_OPL OPL,OPL_PORTHANDLER_WPtr PortHandler_w,OPL_PORTHANDLER_RPtr PortHandler_r,int param)
   {
   	OPL.porthandler_w = PortHandler_w;
   	OPL.porthandler_r = PortHandler_r;
   	OPL.port_param = param;
   }
   
   public static void OPLSetKeyboardHandler(FM_OPL OPL,OPL_PORTHANDLER_WPtr KeyboardHandler_w,OPL_PORTHANDLER_RPtr KeyboardHandler_r,int param)
   {
   	OPL.keyboardhandler_w = KeyboardHandler_w;
   	OPL.keyboardhandler_r = KeyboardHandler_r;
   	OPL.keyboard_param = param;
   }
   /* ---------- YM3812 I/O interface ---------- */

    public static int OPLWrite(FM_OPL OPL, int a, int v) {
        if ((a & 1) == 0) {	/* address port */

            OPL.address = v & 0xff;
        } else {	/* data port */

            if (OPL.UpdateHandler != null) {
                OPL.UpdateHandler.handler(OPL.UpdateParam, 0);
            }
            OPLWriteReg(OPL, OPL.address, v);
        }
        return (OPL.status >> 7) & 0xFF; //status is uint8

    }

    public static /*unsigned*/ char OPLRead(FM_OPL OPL, int a) {
        if ((a & 1) == 0) {	/* status port */

            return (char) ((OPL.status & (OPL.statusmask | 0x80)) & 0xFF);
        }
        /* data port */
        switch (OPL.address) {
            case 0x05: /* KeyBoard IN */
        	if((OPL.type&OPL_TYPE_KEYBOARD)!=0)
   		{
   			if(OPL.keyboardhandler_r!=null)
                        {
   				return (char)OPL.keyboardhandler_r.handler(OPL.keyboard_param);
                        }
   			else
                        {
   				//Log(LOG_WAR,"OPL:read unmapped KEYBOARD port\n");
                        }
   		}
   		return 0;
            case 0x19: /* I/O DATA    */
                if((OPL.type&OPL_TYPE_IO)!=0)
   		{
   			if(OPL.porthandler_r!=null)
                        {
   				return (char)OPL.porthandler_r.handler(OPL.port_param);
                        }
   			else
                        {
   				//Log(LOG_WAR,"OPL:read unmapped I/O port\n");
                        }
   		}
   		return 0;
            case 0x1a: /* PCM-DATA    */

                return 0;
        }
        return 0;
    }

    public static int OPLTimerOver(FM_OPL OPL, int c) {
        if (c != 0) {	/* Timer B */

            OPL_STATUS_SET(OPL, 0x20);
        } else {	/* Timer A */

            OPL_STATUS_SET(OPL, 0x40);
            /* CSM mode key,TL controll */
            if ((OPL.mode & 0x80) != 0) {	/* CSM mode total level latch and auto key on */

                int ch;
                if (OPL.UpdateHandler != null) {
                    OPL.UpdateHandler.handler(OPL.UpdateParam, 0);
                }
                for (ch = 0; ch < 9; ch++) {
                    CSMKeyControll(OPL.P_CH[ch]);
                }
            }
        }
        /* reload timer */
        if (OPL.TimerHandler != null) {
            OPL.TimerHandler.handler(OPL.TimerParam + c, (double) OPL.T[c] * OPL.TimerBase);
        }
        return (OPL.status >> 7) & 0xFF; //status is uint8
    }
}
