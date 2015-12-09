package sound;

import arcadeflex.libc.IntSubArray;
import arcadeflex.ptrlib.UShortPtr;
import mame.driverH.WriteHandlerPtr;
import static sound._2203intf.YM2203UpdateRequest;
import sound.fm_c.FM_ST;
import static sound.fmH.*;
import sound.fm_c.FM_CH;
import sound.fm_c.FM_OPN;
import sound.fm_c.FM_SLOT;
import sound.fm_c.YM2151;
import sound.fm_c.YM2203;
import sound.fm_c.YM2610;
import sound.streams.StreamInitPtr;
import static arcadeflex.libc_old.*;
import static sound._2151intf.YM2151UpdateRequest;
import static arcadeflex.ptrlib.*;
import static sound.YM_DELTA_T.*;
import static sound.streams.*;
import static sound._2610intf.*;
import sound.fm_c.ADPCM_CH;
import static arcadeflex.libc_v2.*;
public class fm {
    /*TODO*///#define YM2610B_WARNING
/*TODO*///
/*TODO*////* YM2608 rhythm data is PCM ,not an ADPCM */
/*TODO*///#define YM2608_RHYTHM_PCM
/*TODO*///
/*TODO*///
/*TODO*////***** shared function building option ****/
/*TODO*///#define BUILD_OPN (BUILD_YM2203||BUILD_YM2608||BUILD_YM2610||BUILD_YM2612)
/*TODO*///#define BUILD_OPNB (BUILD_YM2610||BUILD_YM2610B)
/*TODO*///#define BUILD_FM_ADPCMA (BUILD_YM2608||BUILD_OPNB)
/*TODO*///#define BUILD_FM_ADPCMB (BUILD_YM2608||BUILD_OPNB)
/*TODO*///#define BUILD_STEREO (BUILD_YM2608||BUILD_YM2610||BUILD_YM2612||BUILD_YM2151)
/*TODO*///#define BUILD_LFO (BUILD_YM2608||BUILD_YM2610||BUILD_YM2612||BUILD_YM2151)
/*TODO*///
/*TODO*///#if BUILD_FM_ADPCMB
/*TODO*////* include external DELTA-T ADPCM unit */
/*TODO*///  #include "ymdeltat.h"		/* DELTA-T ADPCM UNIT */

    public static final int DELTAT_MIXING_LEVEL = (4); /* DELTA-T ADPCM MIXING LEVEL */
    /*TODO*///#endif
/*TODO*///
/*TODO*////* ------------------------------------------------------------------ */
/*TODO*///#ifdef __RAINE__
/*TODO*///#define INTERNAL_TIMER 			/* use internal timer */
/*TODO*///#endif

    /* -------------------- sound quality define selection --------------------- */
    /* sinwave entries */
    /* used static memory = SIN_ENT * 4 (byte) */
    public static final int SIN_ENT = 2048;

    /* lower bits of envelope counter */
    public static final int ENV_BITS = 16;

    /* envelope output entries */
    public static final int EG_ENT = 4096;
    public static final double EG_STEP = (96.0 / EG_ENT); /* OPL == 0.1875 dB */

    //#if FM_LFO_SUPPORT
    /* LFO table entries */
    public static final int LFO_ENT = 512;
    public static final int LFO_SHIFT = (32 - 9);

    public static final int LFO_RATE = 0x10000;
    //#endif

    /* -------------------- preliminary define section --------------------- */
    /* attack/decay rate time rate */
    public static final int OPM_ARRATE = 399128;
    public static final int OPM_DRRATE = 5514396;
    /* It is not checked , because I haven't YM2203 rate */
    public static final int OPN_ARRATE = OPM_ARRATE;
    public static final int OPN_DRRATE = OPM_DRRATE;

    /* PG output cut off level : 78dB(14bit)? */
    public static final int PG_CUT_OFF = ((int) (78.0 / EG_STEP));
    /* EG output cut off level : 68dB? */
    public static final int EG_CUT_OFF = ((int) (68.0 / EG_STEP));

    public static final int FREQ_BITS = 24;		/* frequency turn          */

    /* PG counter is 21bits @oct.7 */

    public static final int FREQ_RATE = (1 << (FREQ_BITS - 21));
    public static final int TL_BITS = (FREQ_BITS + 2);
    /* OPbit = 14(13+sign) : TL_BITS+1(sign) / output = 16bit */
    public static final int TL_SHIFT = (TL_BITS + 1 - (14 - 16));

    /* output final shift */
    public static final int FM_OUTSB = (TL_SHIFT - FM_OUTPUT_BIT);
    public static final int FM_MAXOUT = ((1 << (TL_SHIFT - 1)) - 1);
    public static final int FM_MINOUT = (-(1 << (TL_SHIFT - 1)));

    /* -------------------- local defines , macros --------------------- */

    /* envelope counter position */
    public static final int EG_AST = 0;					/* start of Attack phase */

    public static final int EG_AED = (EG_ENT << ENV_BITS);	/* end   of Attack phase */

    public static final int EG_DST = EG_AED;				/* start of Decay/Sustain/Release phase */

    public static final int EG_DED = (EG_DST + ((EG_ENT - 1) << ENV_BITS));	/* end   of Decay/Sustain/Release phase */

    public static final int EG_OFF = EG_DED;				/* off */
    /*TODO*///#if FM_SEG_SUPPORT

    public static final int EG_UST = ((2 * EG_ENT) << ENV_BITS);  /* start of SEG UPSISE */

    public static final int EG_UED = ((3 * EG_ENT) << ENV_BITS);  /* end of SEG UPSISE */
    /*TODO*///#endif
/* register number to channel number , slot offset */


    static int OPN_CHAN(int n) {
        return n & 3;
    }

    static int OPN_SLOT(int n) {
        return (n >> 2) & 3;
    }

    static int OPM_CHAN(int n) {
        return n & 7;
    }

    static int OPM_SLOT(int n) {
        return (n >> 3) & 3;
    }

    /* slot number */
    public static final int SLOT1 = 0;
    public static final int SLOT2 = 2;
    public static final int SLOT3 = 1;
    public static final int SLOT4 = 3;

    /* bit0 = Right enable , bit1 = Left enable */
    public static final int OUTD_RIGHT = 1;
    public static final int OUTD_LEFT = 2;
    public static final int OUTD_CENTER = 3;
    /*TODO*///
/*TODO*////* FM timer model */
/*TODO*///#define FM_TIMER_SINGLE (0)
    public static final int FM_TIMER_INTERVAL = (1);

    /*TODO*////* -------------------- tables --------------------- */

    /* sustain lebel table (3db per step) */
    /* 0 - 15: 0, 3, 6, 9,12,15,18,21,24,27,30,33,36,39,42,93 (dB)*/
    static int SC(int db) {
        return (int) ((db * ((3 / EG_STEP) * (1 << ENV_BITS))) + EG_DST);
    }
    static int[] SL_TABLE = {
        SC(0), SC(1), SC(2), SC(3), SC(4), SC(5), SC(6), SC(7),
        SC(8), SC(9), SC(10), SC(11), SC(12), SC(13), SC(14), SC(31)
    };

    /* size of TL_TABLE = sinwave(max cut_off) + cut_off(tl + ksr + envelope + ams) */
    public static final int TL_MAX = (PG_CUT_OFF + EG_CUT_OFF + 1);

    /* TotalLevel : 48 24 12  6  3 1.5 0.75 (dB) */
    /* TL_TABLE[ 0      to TL_MAX          ] : plus  section */
    /* TL_TABLE[ TL_MAX to TL_MAX+TL_MAX-1 ] : minus section */
    static int[] TL_TABLE;
    /* pointers to TL_TABLE with sinwave output offset */
    static IntSubArray[] SIN_TABLE = new IntSubArray[SIN_ENT];
    /*TODO*///
/*TODO*////* envelope output curve table */
/*TODO*///#if FM_SEG_SUPPORT
/*TODO*////* attack + decay + SSG upside + OFF */
/*TODO*///static INT32 ENV_CURVE[3*EG_ENT+1];
/*TODO*///#else
/*TODO*////* attack + decay + OFF */
    public static int[] ENV_CURVE = new int[2 * EG_ENT + 1];
    /*TODO*///#endif
/* envelope counter conversion table when change Decay to Attack phase */
    public static int[] DRAR_TABLE = new int[EG_ENT];

    /*TODO*///#define OPM_DTTABLE OPN_DTTABLE
    static int OPN_DTTABLE[] = {
        /* this table is YM2151 and YM2612 data */
        /* FD=0 */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        /* FD=1 */
        0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2,
        2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 7, 8, 8, 8, 8,
        /* FD=2 */
        1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5,
        5, 6, 6, 7, 8, 8, 9, 10, 11, 12, 13, 14, 16, 16, 16, 16,
        /* FD=3 */
        2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 7,
        8, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 20, 22, 22, 22, 22
    };

    /* multiple table */
    static int ML(double n) {
        return (int) (n * 2);
    }
    static int[] MUL_TABLE = {
        /* 1/2, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,15 */
        ML(0.50), ML(1.00), ML(2.00), ML(3.00), ML(4.00), ML(5.00), ML(6.00), ML(7.00),
        ML(8.00), ML(9.00), ML(10.00), ML(11.00), ML(12.00), ML(13.00), ML(14.00), ML(15.00),
        /* DT2=1 *SQL(2)   */
        ML(0.71), ML(1.41), ML(2.82), ML(4.24), ML(5.65), ML(7.07), ML(8.46), ML(9.89),
        ML(11.30), ML(12.72), ML(14.10), ML(15.55), ML(16.96), ML(18.37), ML(19.78), ML(21.20),
        /* DT2=2 *SQL(2.5) */
        ML(0.78), ML(1.57), ML(3.14), ML(4.71), ML(6.28), ML(7.85), ML(9.42), ML(10.99),
        ML(12.56), ML(14.13), ML(15.70), ML(17.27), ML(18.84), ML(20.41), ML(21.98), ML(23.55),
        /* DT2=3 *SQL(3)   */
        ML(0.87), ML(1.73), ML(3.46), ML(5.19), ML(6.92), ML(8.65), ML(10.38), ML(12.11),
        ML(13.84), ML(15.57), ML(17.30), ML(19.03), ML(20.76), ML(22.49), ML(24.22), ML(25.95)
    };

    /*TODO*///#if FM_LFO_SUPPORT
/*TODO*///
/*TODO*///#define PMS_RATE 0x400
/*TODO*///
/*TODO*////* LFO runtime work */
/*TODO*///static INT32 *LFO_wave;
    public static long/*UINT32*/ lfo_amd;
    public static int lfo_pmd;
    /*TODO*///#endif

    /* Dummy table of Attack / Decay rate ( use when rate == 0 ) */
    static int RATE_0[]
            = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    /* -------------------- state --------------------- */

    /* some globals */
    public static final int TYPE_SSG = 0x01;    /* SSG support          */

    public static final int TYPE_OPN = 0x02;    /* OPN device           */

    public static final int TYPE_LFOPAN = 0x04;    /* OPN type LFO and PAN */

    public static final int TYPE_6CH = 0x08;    /* FM 6CH / 3CH         */

    public static final int TYPE_DAC = 0x10;    /* YM2612's DAC device  */

    public static final int TYPE_ADPCM = 0x20;    /* two ADPCM unit       */

    public static final int TYPE_YM2203 = (TYPE_SSG);
    /*TODO*///#define TYPE_YM2608 (TYPE_SSG |TYPE_LFOPAN |TYPE_6CH |TYPE_ADPCM)
    public static final int TYPE_YM2610 = (TYPE_SSG | TYPE_LFOPAN | TYPE_6CH | TYPE_ADPCM);
    /*TODO*///#define TYPE_YM2612 (TYPE_6CH |TYPE_LFOPAN |TYPE_DAC)
/*TODO*///
/*TODO*////* current chip state */
    static Object cur_chip = null;		/* pointer of current chip struct */

    static FM_ST State;			/* basic status */

    static FM_CH[] cch = new FM_CH[8];			/* pointer of FM channels */
    /*TODO*///#if FM_LFO_SUPPORT
/*TODO*///static UINT32 LFOCnt,LFOIncr;	/* LFO PhaseGenerator */
/*TODO*///#endif
/*TODO*///
/*TODO*////* runtime work */

    static int[] out_ch = new int[4];			/* channel output NONE,LEFT,RIGHT or CENTER */

    static int[] pg_in1 = new int[1], pg_in2 = new int[1], pg_in3 = new int[1], pg_in4 = new int[1];	/* PG input of SLOTs */
    /*TODO*///
/*TODO*////* log output level */
/*TODO*///#define LOG_ERR  3      /* ERROR       */
/*TODO*///#define LOG_WAR  2      /* WARNING     */
/*TODO*///#define LOG_INF  1      /* INFORMATION */
/*TODO*///
/*TODO*///#define LOG_LEVEL LOG_INF
/*TODO*///
/*TODO*///#ifndef __RAINE__
/*TODO*///static void CLIB_DECL Log(int level,char *format,...)
/*TODO*///{
/*TODO*///	va_list argptr;
/*TODO*///
/*TODO*///	if( level < LOG_LEVEL ) return;
/*TODO*///	va_start(argptr,format);
/*TODO*///	/* */
/*TODO*///	if (errorlog) vfprintf( errorlog, format , argptr);
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*////* ----- limitter ----- */
/*TODO*///#define Limit(val, max,min) { \
/*TODO*///	if ( val > max )      val = max; \
/*TODO*///	else if ( val < min ) val = min; \
/*TODO*///}
/*TODO*///
/*TODO*////* ----- buffering one of data(STEREO chip) ----- */
/*TODO*///#ifdef FM_STEREO_MIX
/*TODO*////* stereo mixing */
/*TODO*///#define FM_BUFFERING_STEREO \
/*TODO*///{														\
/*TODO*///	/* get left & right output with clipping */			\
/*TODO*///	out_ch[OUTD_LEFT]  += out_ch[OUTD_CENTER];				\
/*TODO*///	Limit( out_ch[OUTD_LEFT] , FM_MAXOUT, FM_MINOUT );	\
/*TODO*///	out_ch[OUTD_RIGHT] += out_ch[OUTD_CENTER];				\
/*TODO*///	Limit( out_ch[OUTD_RIGHT], FM_MAXOUT, FM_MINOUT );	\
/*TODO*///	/* buffering */										\
/*TODO*///	((FMSAMPLE_MIX *)bufL)[i] = ((out_ch[OUTD_LEFT]>>FM_OUTSB)<<FM_OUTPUT_BIT)|(out_ch[OUTD_RIGHT]>>FM_OUTSB); \
/*TODO*///}
/*TODO*///#else
/*TODO*////* stereo separate */
/*TODO*///#define FM_BUFFERING_STEREO \
/*TODO*///{														\
/*TODO*///	/* get left & right output with clipping */			\
/*TODO*///	out_ch[OUTD_LEFT]  += out_ch[OUTD_CENTER];				\
/*TODO*///	Limit( out_ch[OUTD_LEFT] , FM_MAXOUT, FM_MINOUT );	\
/*TODO*///	out_ch[OUTD_RIGHT] += out_ch[OUTD_CENTER];				\
/*TODO*///	Limit( out_ch[OUTD_RIGHT], FM_MAXOUT, FM_MINOUT );	\
/*TODO*///	/* buffering */										\
/*TODO*///	bufL[i] = out_ch[OUTD_LEFT] >>FM_OUTSB;				\
/*TODO*///	bufR[i] = out_ch[OUTD_RIGHT]>>FM_OUTSB;				\
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*///#ifdef INTERNAL_TIMER
/*TODO*////* ----- internal timer mode , update timer */
/*TODO*////* ---------- calcrate timer A ---------- */
/*TODO*///#define INTERNAL_TIMER_A(ST,CSM_CH)					\
/*TODO*///{													\
/*TODO*///	if( ST->TAC &&  (ST->Timer_Handler==0) )		\
/*TODO*///		if( (ST->TAC -= ST->freqbase*4096) <= 0 )	\
/*TODO*///		{											\
/*TODO*///			TimerAOver( ST );						\
/*TODO*///			/* CSM mode total level latch and auto key on */	\
/*TODO*///			if( ST->mode & 0x80 )					\
/*TODO*///				CSMKeyControll( CSM_CH );			\
/*TODO*///		}											\
/*TODO*///}
/*TODO*////* ---------- calcrate timer B ---------- */
/*TODO*///#define INTERNAL_TIMER_B(ST,step)						\
/*TODO*///{														\
/*TODO*///	if( ST->TBC && (ST->Timer_Handler==0) )				\
/*TODO*///		if( (ST->TBC -= ST->freqbase*4096*step) <= 0 )	\
/*TODO*///			TimerBOver( ST );							\
/*TODO*///}
/*TODO*///#else
    /* external timer mode */


    static void INTERNAL_TIMER_A(FM_ST ST, FM_CH CSM_CH) {

    }

    static void INTERNAL_TIMER_B(FM_ST ST, int step) {

    }

    /* --------------------- subroutines  --------------------- */
    /* status set and IRQ handling */
    static void FM_STATUS_SET(FM_ST ST, int flag) {
        /* set status flag */
        ST.status |= flag;
        if (((ST.irq) == 0) && ((ST.status & ST.irqmask) != 0)) {
            ST.irq = 1;
            /* callback user interrupt handler (IRQ is OFF to ON) */
            if (ST.IRQ_Handler != null) {
                (ST.IRQ_Handler).handler(ST.index, 1);
            }
        }
    }

    /* status reset and IRQ handling */
    static void FM_STATUS_RESET(FM_ST ST, int flag) {
        /* reset status flag */
        ST.status &= ~flag;
        if (((ST.irq) != 0) && ((ST.status & ST.irqmask) == 0)) {
            ST.irq = 0;
            /* callback user interrupt handler (IRQ is ON to OFF) */
            if (ST.IRQ_Handler != null) {
                (ST.IRQ_Handler).handler(ST.index, 0);
            }
        }
    }

    /* IRQ mask set */
    static void FM_IRQMASK_SET(FM_ST ST, int flag) {
        ST.irqmask = flag;
        /* IRQ handling check */
        FM_STATUS_SET(ST, 0);
        FM_STATUS_RESET(ST, 0);
    }

    /* ---------- event hander of Phase Generator ---------- */
    /* Release end -> stop counter */
    public static EGPtr FM_EG_Release = new EGPtr() {

        @Override
        public void handler(FM_SLOT SLOT) {
            SLOT.evc = EG_OFF;
            SLOT.eve = EG_OFF + 1;
            SLOT.evs = 0;
        }
    };
    /* SUSTAIN end -> stop counter */
    public static EGPtr FM_EG_SR = new EGPtr() {

        @Override
        public void handler(FM_SLOT SLOT) {
            SLOT.evc = EG_OFF;
            SLOT.eve = EG_OFF + 1;
            SLOT.evs = 0;
        }
    };
    /* Decay end -> Sustain */
    public static EGPtr FM_EG_DR = new EGPtr() {

        @Override
        public void handler(FM_SLOT SLOT) {
            SLOT.eg_next = FM_EG_SR;
            SLOT.evc = SLOT.SL;
            SLOT.eve = EG_DED;
            SLOT.evs = SLOT.evss;

        }
    };
    /* Attack end -> Decay */
    public static EGPtr FM_EG_AR = new EGPtr() {

        @Override
        public void handler(FM_SLOT SLOT) {
            /* next DR */
            SLOT.eg_next = FM_EG_DR;
            SLOT.evc = EG_DST;
            SLOT.eve = SLOT.SL;
            SLOT.evs = SLOT.evsd;

        }
    };
    /*TODO*///
/*TODO*///#if FM_SEG_SUPPORT
/*TODO*///static void FM_EG_SSG_SR( FM_SLOT *SLOT );
/*TODO*///
/*TODO*////* SEG down side end  */
/*TODO*///static void FM_EG_SSG_DR( FM_SLOT *SLOT )
/*TODO*///{
/*TODO*///	if( SLOT->SEG&2){
/*TODO*///		/* reverce */
/*TODO*///		SLOT->eg_next = FM_EG_SSG_SR;
/*TODO*///		SLOT->evc = SLOT->SL + (EG_UST - EG_DST);
/*TODO*///		SLOT->eve = EG_UED;
/*TODO*///		SLOT->evs = SLOT->evss;
/*TODO*///	}else{
/*TODO*///		/* again */
/*TODO*///		SLOT->evc = EG_DST;
/*TODO*///	}
/*TODO*///	/* hold */
/*TODO*///	if( SLOT->SEG&1) SLOT->evs = 0;
/*TODO*///}
/*TODO*///
/*TODO*////* SEG upside side end */
/*TODO*///static void FM_EG_SSG_SR( FM_SLOT *SLOT )
/*TODO*///{
/*TODO*///	if( SLOT->SEG&2){
/*TODO*///		/* reverce  */
/*TODO*///		SLOT->eg_next = FM_EG_SSG_DR;
/*TODO*///		SLOT->evc = EG_DST;
/*TODO*///		SLOT->eve = EG_DED;
/*TODO*///		SLOT->evs = SLOT->evsd;
/*TODO*///	}else{
/*TODO*///		/* again */
/*TODO*///		SLOT->evc = SLOT->SL + (EG_UST - EG_DST);
/*TODO*///	}
/*TODO*///	/* hold check */
/*TODO*///	if( SLOT->SEG&1) SLOT->evs = 0;
/*TODO*///}
/*TODO*///
/*TODO*////* SEG Attack end */
/*TODO*///static void FM_EG_SSG_AR( FM_SLOT *SLOT )
/*TODO*///{
/*TODO*///	if( SLOT->SEG&4){	/* start direction */
/*TODO*///		/* next SSG-SR (upside start ) */
/*TODO*///		SLOT->eg_next = FM_EG_SSG_SR;
/*TODO*///		SLOT->evc = SLOT->SL + (EG_UST - EG_DST);
/*TODO*///		SLOT->eve = EG_UED;
/*TODO*///		SLOT->evs = SLOT->evss;
/*TODO*///	}else{
/*TODO*///		/* next SSG-DR (downside start ) */
/*TODO*///		SLOT->eg_next = FM_EG_SSG_DR;
/*TODO*///		SLOT->evc = EG_DST;
/*TODO*///		SLOT->eve = EG_DED;
/*TODO*///		SLOT->evs = SLOT->evsd;
/*TODO*///	}
/*TODO*///}
/*TODO*///#endif /* FM_SEG_SUPPORT */
/*TODO*///
    /* ----- key on of SLOT ----- */

    static boolean FM_KEY_IS(FM_SLOT SLOT) {
        return (SLOT.eg_next != FM_EG_Release);
    }

    static void FM_KEYON(FM_CH CH, int s) {
        FM_SLOT SLOT = CH.SLOT[s];
        if (!FM_KEY_IS(SLOT)) {
            /* restart Phage Generator */
            SLOT.Cnt = 0;
            /* phase -> Attack */
            /*TODO*///#if FM_SEG_SUPPORT
/*TODO*///		if( SLOT->SEG&8 ) SLOT->eg_next = FM_EG_SSG_AR;
/*TODO*///		else
/*TODO*///#endif
            SLOT.eg_next = FM_EG_AR;
            SLOT.evs = SLOT.evsa;
            /* reset attack counter */
            SLOT.evc = EG_AST;
            SLOT.eve = EG_AED;
        }
    }
    /* ----- key off of SLOT ----- */

    static void FM_KEYOFF(FM_CH CH, int s) {
        FM_SLOT SLOT = CH.SLOT[s];
        if (FM_KEY_IS(SLOT)) {
            /* if Attack phase then adjust envelope counter */
            if (SLOT.evc < EG_DST) {
                SLOT.evc = (ENV_CURVE[SLOT.evc >> ENV_BITS] << ENV_BITS) + EG_DST;
            }
            /* phase -> Release */
            SLOT.eg_next = FM_EG_Release;
            SLOT.eve = EG_DED;
            SLOT.evs = SLOT.evsr;
        }
    }

    /*TODO*////* setup Algorythm and PAN connection */
    static void setup_connection(FM_CH CH) {
        IntSubArray carrier = new IntSubArray(out_ch, CH.PAN); /* NONE,LEFT,RIGHT or CENTER */

        switch (CH.ALGO) {
            case 0:
                /*  PG---S1---S2---S3---S4---OUT */
                CH.connect1 = new IntSubArray(pg_in2);
                CH.connect2 = new IntSubArray(pg_in3);
                CH.connect3 = new IntSubArray(pg_in4);
                break;
            case 1:
                /*  PG---S1-+-S3---S4---OUT */
                /*  PG---S2-+               */
                CH.connect1 = new IntSubArray(pg_in3);
                CH.connect2 = new IntSubArray(pg_in3);
                CH.connect3 = new IntSubArray(pg_in4);
                break;
            case 2:
                /* PG---S1------+-S4---OUT */
                /* PG---S2---S3-+          */
                CH.connect1 = new IntSubArray(pg_in4);
                CH.connect2 = new IntSubArray(pg_in3);
                CH.connect3 = new IntSubArray(pg_in4);
                break;
            case 3:
                /* PG---S1---S2-+-S4---OUT */
                /* PG---S3------+          */
                CH.connect1 = new IntSubArray(pg_in2);
                CH.connect2 = new IntSubArray(pg_in4);
                CH.connect3 = new IntSubArray(pg_in4);
                break;
            case 4:
                /* PG---S1---S2-+--OUT */
                /* PG---S3---S4-+      */
                CH.connect1 = new IntSubArray(pg_in2);
                CH.connect2 = carrier;
                CH.connect3 = new IntSubArray(pg_in4);
                break;
            case 5:
                /*         +-S2-+     */
                /* PG---S1-+-S3-+-OUT */
                /*         +-S4-+     */
                CH.connect1 = null;	/* special case */

                CH.connect2 = carrier;
                CH.connect3 = carrier;
                break;
            case 6:
                /* PG---S1---S2-+     */
                /* PG--------S3-+-OUT */
                /* PG--------S4-+     */
                CH.connect1 = new IntSubArray(pg_in2);
                CH.connect2 = carrier;
                CH.connect3 = carrier;
                break;
            case 7:
                /* PG---S1-+     */
                /* PG---S2-+-OUT */
                /* PG---S3-+     */
                /* PG---S4-+     */
                CH.connect1 = carrier;
                CH.connect2 = carrier;
                CH.connect3 = carrier;
                break;
        }
        CH.connect4 = carrier;
    }

    /* set detune & multiple */
    static void set_det_mul(FM_ST ST, FM_CH CH, FM_SLOT SLOT, int v) {
        SLOT.mul = MUL_TABLE[v & 0x0f];
        SLOT.DT = ST.DT_TABLE[(v >> 4) & 7];
        CH.SLOT[SLOT1].Incr = -1;
    }

    /* set total level */
    static void set_tl(FM_CH CH, FM_SLOT SLOT, int v, int csmflag) {
        v &= 0x7f;
        v = (v << 7) | v; /* 7bit -> 14bit */

        SLOT.TL = (v * EG_ENT) >> 14;
        if (csmflag == 0) {	/* not CSM latch total level */

            SLOT.TLL = SLOT.TL /* + KSL[CH->kcode] */;
        }
    }

    /* set attack rate & key scale  */
    static void set_ar_ksr(FM_CH CH, FM_SLOT SLOT, int v, IntSubArray ar_table) {
        SLOT.KSR = (3 - (v >> 6));
        SLOT.AR = (v &= 0x1f) != 0 ? new IntSubArray(ar_table, v << 1) : new IntSubArray(RATE_0);
        SLOT.evsa = SLOT.AR.read(SLOT.ksr);
        if (SLOT.eg_next == FM_EG_AR) {
            SLOT.evs = SLOT.evsa;
        }

    }
    /* set decay rate */

    static void set_dr(FM_SLOT SLOT, int v, IntSubArray dr_table) {
        SLOT.DR = (v &= 0x1f) != 0 ? new IntSubArray(dr_table, v << 1) : new IntSubArray(RATE_0);
        SLOT.evsd = SLOT.DR.read(SLOT.ksr);
        if (SLOT.eg_next == FM_EG_DR) {
            SLOT.evs = SLOT.evsd;
        }
    }
    /* set sustain rate */

    static void set_sr(FM_SLOT SLOT, int v, IntSubArray dr_table) {
        SLOT.SR = (v &= 0x1f) != 0 ? new IntSubArray(dr_table, v << 1) : new IntSubArray(RATE_0);
        SLOT.evss = SLOT.SR.read(SLOT.ksr);
        if (SLOT.eg_next == FM_EG_SR) {
            SLOT.evs = SLOT.evss;
        }
    }
    /* set release rate */

    static void set_sl_rr(FM_SLOT SLOT, int v, IntSubArray dr_table) {
        SLOT.SL = SL_TABLE[(v >> 4)];
        SLOT.RR = new IntSubArray(dr_table, ((v & 0x0f) << 2) | 2);
        SLOT.evsr = SLOT.RR.read(SLOT.ksr);
        if (SLOT.eg_next == FM_EG_Release) {
            SLOT.evs = SLOT.evsr;
        }
    }

    /* operator output calcrator */
    static int OP_OUT(int PG, int EG) {
        return SIN_TABLE[(PG / (0x1000000 / SIN_ENT)) & (SIN_ENT - 1)].read(EG);
    }

    static int OP_OUTN(int PG, int EG) {
        return NOISE_TABLE[(int) ((PG / (0x1000000 / SIN_ENT)) & (SIN_ENT - 1))].read(EG);
    }

    public static int FM_CALC_EG(FM_SLOT SLOT) {
        if ((SLOT.evc += SLOT.evs) >= SLOT.eve) {
            SLOT.eg_next.handler(SLOT);
        }
        int OUT = SLOT.TLL + ENV_CURVE[SLOT.evc >> ENV_BITS];
        if (SLOT.ams != 0) {
            OUT += (SLOT.ams * lfo_amd / LFO_RATE);
        }
        return OUT;
    }
    /* ---------- calcrate one of channel ---------- */

    public static void FM_CALC_CH(FM_CH CH) {
        long/*UINT32*/ eg_out1, eg_out2, eg_out3, eg_out4;  //envelope output
/*TODO*///
/*TODO*///	/* Phase Generator */
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	INT32 pms = lfo_pmd * CH->pms / LFO_RATE;
/*TODO*///	if(pms)
/*TODO*///	{
/*TODO*///		pg_in1 = (CH->SLOT[SLOT1].Cnt += CH->SLOT[SLOT1].Incr + (INT32)(pms * CH->SLOT[SLOT1].Incr) / PMS_RATE);
/*TODO*///		pg_in2 = (CH->SLOT[SLOT2].Cnt += CH->SLOT[SLOT2].Incr + (INT32)(pms * CH->SLOT[SLOT2].Incr) / PMS_RATE);
/*TODO*///		pg_in3 = (CH->SLOT[SLOT3].Cnt += CH->SLOT[SLOT3].Incr + (INT32)(pms * CH->SLOT[SLOT3].Incr) / PMS_RATE);
/*TODO*///		pg_in4 = (CH->SLOT[SLOT4].Cnt += CH->SLOT[SLOT4].Incr + (INT32)(pms * CH->SLOT[SLOT4].Incr) / PMS_RATE);
/*TODO*///	}
/*TODO*///	else
/*TODO*///#endif
/*TODO*///	{
        pg_in1[0] = (int) (CH.SLOT[SLOT1].Cnt += CH.SLOT[SLOT1].Incr);
        pg_in2[0] = (int) (CH.SLOT[SLOT2].Cnt += CH.SLOT[SLOT2].Incr);
        pg_in3[0] = (int) (CH.SLOT[SLOT3].Cnt += CH.SLOT[SLOT3].Incr);
        pg_in4[0] = (int) (CH.SLOT[SLOT4].Cnt += CH.SLOT[SLOT4].Incr);
        /*TODO*///	}

        /* Envelope Generator */
        eg_out1 = FM_CALC_EG(CH.SLOT[SLOT1]);
        eg_out2 = FM_CALC_EG(CH.SLOT[SLOT2]);
        eg_out3 = FM_CALC_EG(CH.SLOT[SLOT3]);
        eg_out4 = FM_CALC_EG(CH.SLOT[SLOT4]);

        /* Connection */
        if (eg_out1 < EG_CUT_OFF) /* SLOT 1 */ {
            if (CH.FB != 0) {
                /* with self feed back */
                pg_in1[0] += (CH.op1_out[0] + CH.op1_out[1]) >> CH.FB;
                CH.op1_out[1] = CH.op1_out[0];
            }
            CH.op1_out[0] = OP_OUT(pg_in1[0], (int) eg_out1);
            /* output slot1 */
            if (CH.connect1 == null) {
                /* algorythm 5  */
                pg_in2[0] += CH.op1_out[0];
                pg_in3[0] += CH.op1_out[0];
                pg_in4[0] += CH.op1_out[0];
            } else {
                /* other algorythm */
                CH.connect1.write(0, CH.connect1.read(0) + CH.op1_out[0]);//*CH->connect1 += CH->op1_out[0];
            }
        }
        if (eg_out2 < EG_CUT_OFF) /* SLOT 2 */ {
            CH.connect2.write(0, CH.connect2.read(0) + OP_OUT(pg_in2[0], (int) eg_out2));//*CH->connect2 += OP_OUT(pg_in2,eg_out2);
        }
        if (eg_out3 < EG_CUT_OFF) /* SLOT 3 */ {
            CH.connect3.write(0, CH.connect3.read(0) + OP_OUT(pg_in3[0], (int) eg_out3));//*CH->connect3 += OP_OUT(pg_in3,eg_out3);
        }
        if (eg_out4 < EG_CUT_OFF) /* SLOT 4 */ {
            CH.connect4.write(0, CH.connect4.read(0) + OP_OUT(pg_in4[0], (int) eg_out4));//*CH->connect4 += OP_OUT(pg_in4,eg_out4);
        }
    }
    /* ---------- frequency counter for operater update ---------- */

    public static void CALC_FCSLOT(FM_SLOT SLOT, int fc, int kc) {
        int ksr;

        /* frequency step counter */
        /* SLOT->Incr= (fc+SLOT->DT[kc])*SLOT->mul; */
        SLOT.Incr = fc * SLOT.mul + SLOT.DT[kc];
        ksr = kc >> SLOT.KSR;
        if (SLOT.ksr != ksr) {
            SLOT.ksr = ksr;
            /* attack , decay rate recalcration */
            SLOT.evsa = SLOT.AR.read(ksr);
            SLOT.evsd = SLOT.DR.read(ksr);
            SLOT.evss = SLOT.SR.read(ksr);
            SLOT.evsr = SLOT.RR.read(ksr);
        }
        SLOT.TLL = SLOT.TL /* + KSL[kc]*/;
    }

    /* ---------- frequency counter  ---------- */
    static void CALC_FCOUNT(FM_CH CH) {
        if (CH.SLOT[SLOT1].Incr == -1) {
            int fc = (int) CH.fc;
            int kc = CH.kcode;
            CALC_FCSLOT(CH.SLOT[SLOT1], fc, kc);
            CALC_FCSLOT(CH.SLOT[SLOT2], fc, kc);
            CALC_FCSLOT(CH.SLOT[SLOT3], fc, kc);
            CALC_FCSLOT(CH.SLOT[SLOT4], fc, kc);
        }
    }

    /* ----------- initialize time tabls ----------- */
    static void init_timetables(FM_ST ST, int[] DTTABLE, int ARRATE, int DRRATE) {
        int i, d;
        double rate;

        /* DeTune table */
        for (d = 0; d <= 3; d++) {
            for (i = 0; i <= 31; i++) {
                rate = (double) DTTABLE[d * 32 + i] * ST.freqbase * FREQ_RATE;
                ST.DT_TABLE[d][i] = (int) rate;
                ST.DT_TABLE[d + 4][i] = (int) -rate;
            }
        }

        /* make Attack & Decay tables */
        for (i = 0; i < 4; i++) {
            ST.AR_TABLE.write(i, 0);
            ST.DR_TABLE.write(i, 0);
        }
        for (i = 4; i < 64; i++) {
            rate = ST.freqbase;						/* frequency rate */

            if (i < 60) {
                rate *= 1.0 + (i & 3) * 0.25;		/* b0-1 : x1 , x1.25 , x1.5 , x1.75 */

            }
            rate *= 1 << ((i >> 2) - 1);						/* b2-5 : shift bit */

            rate *= (double) (EG_ENT << ENV_BITS);
            ST.AR_TABLE.write(i, (int) (rate / ARRATE));
            ST.DR_TABLE.write(i, (int) (rate / DRRATE));
        }

        ST.AR_TABLE.write(62, EG_AED - 1);
        ST.AR_TABLE.write(63, EG_AED - 1);
        for (i = 64; i < 94; i++) {	/* make for overflow area */

            ST.AR_TABLE.write(i, ST.AR_TABLE.read(63));
            ST.DR_TABLE.write(i, ST.DR_TABLE.read(63));
        }

    }

    /* ---------- reset one of channel  ---------- */
    static void reset_channel(FM_ST ST, FM_CH[] CH, int chan) {
        int c, s;

        ST.mode = 0;	/* normal mode */

        FM_STATUS_RESET(ST, 0xff);
        ST.TA = 0;
        ST.TAC = 0;
        ST.TB = 0;
        ST.TBC = 0;

        for (c = 0; c < chan; c++) {
            CH[c].fc = 0;
            CH[c].PAN = OUTD_CENTER;
            for (s = 0; s < 4; s++) {
                CH[c].SLOT[s].SEG = 0;
                CH[c].SLOT[s].eg_next = FM_EG_Release;
                CH[c].SLOT[s].evc = EG_OFF;
                CH[c].SLOT[s].eve = EG_OFF + 1;
                CH[c].SLOT[s].evs = 0;
            }
        }
    }

    /* ---------- generic table initialize ---------- */
    static int FMInitTable() {
        int s, t;
        double rate;
        int i, j;
        double pom;

        /* allocate total level table plus+minus section */
        TL_TABLE = new int[2 * TL_MAX];
        /* make total level table */
        for (t = 0; t < TL_MAX; t++) {
            if (t >= PG_CUT_OFF) {
                rate = 0;	/* under cut off area */

            } else {
                rate = ((1 << TL_BITS) - 1) / Math.pow(10, EG_STEP * t / 20);	/* dB -> voltage */

            }
            TL_TABLE[t] = (int) rate;
            TL_TABLE[TL_MAX + t] = -TL_TABLE[t];
            /*		Log(LOG_INF,"TotalLevel(%3d) = %x\n",t,TL_TABLE[t]);*/
        }

        /* make sinwave table (total level offet) */
        for (s = 1; s <= SIN_ENT / 4; s++) {
            pom = Math.sin(2.0 * Math.PI * s / SIN_ENT); /* sin   */

            pom = 20 * Math.log10(1 / pom);	     /* -> decibel */

            j = (int) (pom / EG_STEP);    /* TL_TABLE steps */
            /* cut off check */

            if (j > PG_CUT_OFF) {
                j = PG_CUT_OFF;
            }
            /* degree 0   -  90    , degree 180 -  90 : plus section */
            SIN_TABLE[s] = SIN_TABLE[SIN_ENT / 2 - s] = new IntSubArray(TL_TABLE, j);
            /* degree 180 - 270    , degree 360 - 270 : minus section */
            SIN_TABLE[SIN_ENT / 2 + s] = SIN_TABLE[SIN_ENT - s] = new IntSubArray(TL_TABLE, TL_MAX + j);
            /* Log(LOG_INF,"sin(%3d) = %f:%f db\n",s,pom,(double)j * EG_STEP); */
        }
        /* degree 0 = degree 180                   = off */
        SIN_TABLE[0] = SIN_TABLE[SIN_ENT / 2] = new IntSubArray(TL_TABLE, PG_CUT_OFF);

        /* envelope counter -> envelope output table */
        for (i = 0; i < EG_ENT; i++) {
            /* ATTACK curve */
            /* !!!!! preliminary !!!!! */
            pom = Math.pow(((double) (EG_ENT - 1 - i) / EG_ENT), 8) * EG_ENT;
            /* if( pom >= EG_ENT ) pom = EG_ENT-1; */
            ENV_CURVE[i] = (int) pom;
            /* DECAY ,RELEASE curve */
            ENV_CURVE[(EG_DST >> ENV_BITS) + i] = i;
            /*TODO*///#if FM_SEG_SUPPORT
/*TODO*///		/* DECAY UPSIDE (SSG ENV) */
/*TODO*///		ENV_CURVE[(EG_UST>>ENV_BITS)+i]= EG_ENT-1-i;
/*TODO*///#endif
        }
        /* off */
        ENV_CURVE[EG_OFF >> ENV_BITS] = EG_ENT - 1;

        /* decay to reattack envelope converttable */
        j = EG_ENT - 1;
        for (i = 0; i < EG_ENT; i++) {
            while (j != 0 && (ENV_CURVE[j] < i)) {
                j--;
            }
            DRAR_TABLE[i] = j << ENV_BITS;
            /* Log(LOG_INF,"DR %06X = %06X,AR=%06X\n",i,DRAR_TABLE[i],ENV_CURVE[DRAR_TABLE[i]>>ENV_BITS] ); */
        }
        return 1;
    }

    static void FMCloseTable() {
        if (TL_TABLE != null) {
            TL_TABLE = null;
        }
    }

    /* OPN/OPM Mode  Register Write */
    static void FMSetMode(FM_ST ST, int n, int v) {
        /* b7 = CSM MODE */
        /* b6 = 3 slot mode */
        /* b5 = reset b */
        /* b4 = reset a */
        /* b3 = timer enable b */
        /* b2 = timer enable a */
        /* b1 = load b */
        /* b0 = load a */
        ST.mode = v;

        /* reset Timer b flag */
        if ((v & 0x20) != 0) {
            FM_STATUS_RESET(ST, 0x02);
        }
        /* reset Timer a flag */
        if ((v & 0x10) != 0) {
            FM_STATUS_RESET(ST, 0x01);
        }
        /* load b */
        if ((v & 0x02) != 0) {
            if (ST.TBC == 0) {
                ST.TBC = (256 - ST.TB) << 4;
                /* External timer handler */
                if (ST.Timer_Handler != null) {
                    (ST.Timer_Handler).handler(n, 1, (double) ST.TBC, ST.TimerBase);
                }
            }
        } else if (ST.timermodel == FM_TIMER_INTERVAL) {	/* stop interbval timer */

            if (ST.TBC != 0) {
                ST.TBC = 0;
                if (ST.Timer_Handler != null) {
                    (ST.Timer_Handler).handler(n, 1, 0, ST.TimerBase);
                }
            }
        }
        /* load a */
        if ((v & 0x01) != 0) {
            if (ST.TAC == 0) {
                ST.TAC = (1024 - ST.TA);
                /* External timer handler */
                if (ST.Timer_Handler != null) {
                    (ST.Timer_Handler).handler(n, 0, (double) ST.TAC, ST.TimerBase);
                }
            }
        } else if (ST.timermodel == FM_TIMER_INTERVAL) {	/* stop interbval timer */

            if (ST.TAC != 0) {
                ST.TAC = 0;
                if (ST.Timer_Handler != null) {
                    (ST.Timer_Handler).handler(n, 0, 0, ST.TimerBase);
                }
            }
        }
    }

    /* Timer A Overflow */
    static void TimerAOver(FM_ST ST) {
        /* status set if enabled */
        if ((ST.mode & 0x04) != 0) {
            FM_STATUS_SET(ST, 0x01);
        }
        /* clear or reload the counter */
        if (ST.timermodel == FM_TIMER_INTERVAL) {
            ST.TAC = (1024 - ST.TA);
            if (ST.Timer_Handler != null) {
                (ST.Timer_Handler).handler(ST.index, 0, (double) ST.TAC, ST.TimerBase);
            }
        } else {
            ST.TAC = 0;
        }
    }
    /* Timer B Overflow */

    static void TimerBOver(FM_ST ST) {
        /* status set if enabled */
        if ((ST.mode & 0x08) != 0) {
            FM_STATUS_SET(ST, 0x02);
        }
        /* clear or reload the counter */
        if (ST.timermodel == FM_TIMER_INTERVAL) {
            ST.TBC = (256 - ST.TB) << 4;
            if (ST.Timer_Handler != null) {
                (ST.Timer_Handler).handler(ST.index, 1, (double) ST.TBC, ST.TimerBase);
            }
        } else {
            ST.TBC = 0;
        }
    }
    /* CSM Key Controll */

    static void CSMKeyControll(FM_CH CH) {
        /* int ksl = KSL[CH->kcode]; */
        /* all key off */
        FM_KEYOFF(CH, SLOT1);
        FM_KEYOFF(CH, SLOT2);
        FM_KEYOFF(CH, SLOT3);
        FM_KEYOFF(CH, SLOT4);
        /* total level latch */
        CH.SLOT[SLOT1].TLL = CH.SLOT[SLOT1].TL /*+ ksl*/;
        CH.SLOT[SLOT2].TLL = CH.SLOT[SLOT2].TL /*+ ksl*/;
        CH.SLOT[SLOT3].TLL = CH.SLOT[SLOT3].TL /*+ ksl*/;
        CH.SLOT[SLOT4].TLL = CH.SLOT[SLOT4].TL /*+ ksl*/;
        /* all key on */
        FM_KEYON(CH, SLOT1);
        FM_KEYON(CH, SLOT2);
        FM_KEYON(CH, SLOT3);
        FM_KEYON(CH, SLOT4);
    }

    /*TODO*///#if BUILD_OPN
/*TODO*////***********************************************************/
/*TODO*////* OPN unit                                                */
/*TODO*////***********************************************************/
/*TODO*///

    /* OPN key frequency number -> key code follow table */
    /* fnum higher 4bit -> keycode lower 2bit */
    static int OPN_FKTABLE[] = {0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 3, 3, 3, 3, 3, 3};

    /* ---------- priscaler set(and make time tables) ---------- */
    static void OPNSetPris(FM_OPN OPN, int pris, int TimerPris, int SSGpris) {
        int i;

        /* frequency base */
        OPN.ST.freqbase = (OPN.ST.rate) != 0 ? ((double) OPN.ST.clock / OPN.ST.rate) / pris : 0;
        /* Timer base time */
        OPN.ST.TimerBase = 1.0 / ((double) OPN.ST.clock / (double) TimerPris);
        /* SSG part  priscaler set */
        if (SSGpris != 0) {
            SSGClk(OPN.ST.index, OPN.ST.clock * 2 / SSGpris);
        }
        /* make time tables */
        init_timetables(OPN.ST, OPN_DTTABLE, OPN_ARRATE, OPN_DRRATE);
        /* make fnumber -> increment counter table */
        for (i = 0; i < 2048; i++) {
            /* it is freq table for octave 7 */
            /* opn freq counter = 20bit */
            OPN.FN_TABLE[i] = (long) ((double) i * OPN.ST.freqbase * FREQ_RATE * (1 << 7) / 2);
        }
//#if FM_LFO_SUPPORT
	/* LFO wave table */
        for (i = 0; i < LFO_ENT; i++) {
            OPN.LFO_wave[i] = i < LFO_ENT / 2 ? i * LFO_RATE / (LFO_ENT / 2) : (LFO_ENT - i) * LFO_RATE / (LFO_ENT / 2);
        }
        /* LFO freq. table */
        {
            /* 3.98Hz,5.56Hz,6.02Hz,6.37Hz,6.88Hz,9.63Hz,48.1Hz,72.2Hz @ 8MHz */
            double freq_table[] = {3.98, 5.56, 6.02, 6.37, 6.88, 9.63, 48.1, 72.2};
            for (i = 0; i < 8; i++) {
                OPN.LFO_FREQ[i] = ((long) ((OPN.ST.rate) != 0 ? ((double) LFO_ENT * (1 << LFO_SHIFT)
                        / (OPN.ST.rate / freq_table[i]
                        * (OPN.ST.freqbase * OPN.ST.rate / (8000000.0 / 144)))) : 0));
            }
        }
//#endif
/*	Log(LOG_INF,"OPN %d set priscaler %d\n",OPN->ST.index,pris);*/
    }

    /* ---------- write a OPN mode register 0x20-0x2f ---------- */
    static void OPNWriteMode(FM_OPN OPN, int r, int v) {
        /*UINT8*/
        int c;
        FM_CH CH;

        switch (r) {
            case 0x21:	/* Test */

                break;
//#if FM_LFO_SUPPORT
            case 0x22:	/* LFO FREQ (YM2608/YM2612) */

                /*TODO*///                if ((OPN.type & TYPE_LFOPAN) != 0) {
/*TODO*///                    throw new UnsupportedOperationException("Unsupported");
                    /*TODO*///			OPN->LFOIncr = (v&0x08) ? OPN->LFO_FREQ[v&7] : 0;
/*TODO*///			cur_chip = NULL;
 /*TODO*///               }
                break;
//#endif
            case 0x24:	/* timer A High 8*/

                OPN.ST.TA = (OPN.ST.TA & 0x03) | (((int) v) << 2);
                break;
            case 0x25:	/* timer A Low 2*/

                OPN.ST.TA = (OPN.ST.TA & 0x3fc) | (v & 3);
                break;
            case 0x26:	/* timer B */

                OPN.ST.TB = v;
                break;
            case 0x27:	/* mode , timer controll */

                FMSetMode((OPN.ST), OPN.ST.index, v);
                break;
            case 0x28:	/* key on / off */

                c = v & 0x03;
                if (c == 3) {
                    break;
                }
                if (((v & 0x04) != 0) && ((OPN.type & TYPE_6CH) != 0)) {
                    c += 3;
                }
                CH = OPN.P_CH[c];
                //CH = CH[c];
		/* csm mode */
                if (c == 2 && ((OPN.ST.mode & 0x80) != 0)) {
                    break;
                }
                if ((v & 0x10) != 0) {
                    FM_KEYON(CH, SLOT1);
                } else {
                    FM_KEYOFF(CH, SLOT1);
                }
                if ((v & 0x20) != 0) {
                    FM_KEYON(CH, SLOT2);
                } else {
                    FM_KEYOFF(CH, SLOT2);
                }
                if ((v & 0x40) != 0) {
                    FM_KEYON(CH, SLOT3);
                } else {
                    FM_KEYOFF(CH, SLOT3);
                }
                if ((v & 0x80) != 0) {
                    FM_KEYON(CH, SLOT4);
                } else {
                    FM_KEYOFF(CH, SLOT4);
                }
                /*		Log(LOG_INF,"OPN %d:%d : KEY %02X\n",n,c,v&0xf0);*/
                break;
        }
    }

    /* ---------- write a OPN register (0x30-0xff) ---------- */
    static void OPNWriteReg(FM_OPN OPN, int r, int v) {
        int/*UINT8*/ c;
        FM_CH CH;
        FM_SLOT SLOT;

        /* 0x30 - 0xff */
        if ((c = OPN_CHAN(r)) == 3) {
            return; /* 0xX3,0xX7,0xXB,0xXF */

        }
        if ((r >= 0x100) /* && (OPN->type & TYPE_6CH) */) {
            c += 3;
        }
        CH = OPN.P_CH[c];
        //CH = &CH[c];

        SLOT = (CH.SLOT[OPN_SLOT(r)]);
        switch (r & 0xf0) {
            case 0x30:	/* DET , MUL */

                set_det_mul(OPN.ST, CH, SLOT, v);
                break;
            case 0x40:	/* TL */

                set_tl(CH, SLOT, v, ((c == 2) && ((OPN.ST.mode & 0x80) != 0)) ? 1 : 0);
                break;
            case 0x50:	/* KS, AR */

                set_ar_ksr(CH, SLOT, v, OPN.ST.AR_TABLE);
                break;
            case 0x60:	/*     DR */
                /* bit7 = AMS_ON ENABLE(YM2612) */

                set_dr(SLOT, v, OPN.ST.DR_TABLE);
//#if FM_LFO_SUPPORT
 /*TODO*///               if ((OPN.type & TYPE_LFOPAN) != 0) {
 /*TODO*///                    throw new UnsupportedOperationException("Unsupported");
                    /*TODO*///			SLOT->amon = v>>7;
/*TODO*///			SLOT->ams = CH->ams * SLOT->amon;
  /*TODO*///               }
//#endif
                break;
            case 0x70:	/*     SR */

                set_sr(SLOT, v, OPN.ST.DR_TABLE);
                break;
            case 0x80:	/* SL, RR */

                set_sl_rr(SLOT, v, OPN.ST.DR_TABLE);
                break;
            case 0x90:	/* SSG-EG */
                /*TODO*///#if !FM_SEG_SUPPORT
/*TODO*///		if(v&0x08) Log(LOG_ERR,"OPN %d,%d,%d :SSG-TYPE envelope selected (not supported )\n",OPN->ST.index,c,OPN_SLOT(r));
/*TODO*///#endif

                SLOT.SEG = v & 0x0f;
                break;
            case 0xa0:
                switch (OPN_SLOT(r)) {
                    case 0: /* 0xa0-0xa2 : FNUM1 */ {
                        long fn = (((long) ((CH.fn_h) & 7)) << 8) + v;
                        int blk = CH.fn_h >> 3;
                        /* make keyscale code */
                        CH.kcode = (blk << 2) | OPN_FKTABLE[(int) (fn >> 7)];
                        /* make basic increment counter 32bit = 1 cycle */
                        CH.fc = OPN.FN_TABLE[(int) fn] >> (7 - blk);
                        CH.SLOT[SLOT1].Incr = -1;
                    }
                    break;
                    case 1:		/* 0xa4-0xa6 : FNUM2,BLK */

                        CH.fn_h = v & 0x3f;
                        break;
                    case 2:		/* 0xa8-0xaa : 3CH FNUM1 */

                        if (r < 0x100) {
                            long fn = (((long) (OPN.SL3.fn_h[c] & 7)) << 8) + v;
                            int blk = OPN.SL3.fn_h[c] >> 3;
                            /* make keyscale code */
                            OPN.SL3.kcode[c] = (blk << 2) | OPN_FKTABLE[(int) (fn >> 7)];
                            /* make basic increment counter 32bit = 1 cycle */
                            OPN.SL3.fc[c] = OPN.FN_TABLE[(int) fn] >> (7 - blk);
                            (OPN.P_CH)[2].SLOT[SLOT1].Incr = -1;
                        }
                        break;
                    case 3:		/* 0xac-0xae : 3CH FNUM2,BLK */

                        if (r < 0x100) {
                            OPN.SL3.fn_h[c] = v & 0x3f;
                        }
                        break;
                }
                break;
            case 0xb0:
                switch (OPN_SLOT(r)) {
                    case 0: /* 0xb0-0xb2 : FB,ALGO */ {
                        int feedback = (v >> 3) & 7;
                        CH.ALGO = v & 7;
                        CH.FB = feedback != 0 ? 8 + 1 - feedback : 0;
                        setup_connection(CH);
                    }
                    break;
                    case 1:		/* 0xb4-0xb6 : L , R , AMS , PMS (YM2612/YM2608) */

                        if ((OPN.type & TYPE_LFOPAN) != 0) {
                            /*TODO*///#if FM_LFO_SUPPORT
/*TODO*///				/* b0-2 PMS */
/*TODO*///				/* 0,3.4,6.7,10,14,20,40,80(cent) */
/*TODO*///				static const double pmd_table[8]={0,3.4,6.7,10,14,20,40,80};
/*TODO*///				static const int amd_table[4]={0/EG_STEP,1.4/EG_STEP,5.9/EG_STEP,11.8/EG_STEP };
/*TODO*///				CH->pms = (1.5/1200.0)*pmd_table[(v>>4) & 0x07] * PMS_RATE;
/*TODO*///				/* b4-5 AMS */
/*TODO*///				/* 0 , 1.4 , 5.9 , 11.8(dB) */
/*TODO*///				CH->ams = amd_table[(v>>4) & 0x03];
/*TODO*///				CH->SLOT[SLOT1].ams = CH->ams * CH->SLOT[SLOT1].amon;
/*TODO*///				CH->SLOT[SLOT2].ams = CH->ams * CH->SLOT[SLOT2].amon;
/*TODO*///				CH->SLOT[SLOT3].ams = CH->ams * CH->SLOT[SLOT3].amon;
/*TODO*///				CH->SLOT[SLOT4].ams = CH->ams * CH->SLOT[SLOT4].amon;
/*TODO*///#endif
				/* PAN */
                            CH.PAN = (v >> 6) & 0x03; /* PAN : b6 = R , b7 = L */

                            setup_connection(CH);
                            /*TODO*///				/* Log(LOG_INF,"OPN %d,%d : PAN %d\n",n,c,CH->PAN);*/
                        }
                        break;
                }
                break;
        }
    }
    /*TODO*///#endif /* BUILD_OPN */
/*TODO*///
/*TODO*///#if BUILD_YM2203
/*TODO*////*******************************************************************************/
/*TODO*////*		YM2203 local section                                                   */
/*TODO*////*******************************************************************************/

    static YM2203[] FM2203 = null;	/* array of YM2203's */

    static int YM2203NumChips;	/* total chip */

    /* ---------- update one of chip ----------- */
    public static StreamInitPtr YM2203UpdateOne = new StreamInitPtr() {
        public void handler(int num, ShortPtr buffer, int length) {
            YM2203 F2203 = (FM2203[num]);
            FM_OPN OPN = (FM2203[num].OPN);
            int i;
            FM_CH ch;
            ShortPtr buf = new ShortPtr(buffer);

            cur_chip = F2203;
            State = F2203.OPN.ST;
            cch[0] = F2203.CH[0];
            cch[1] = F2203.CH[1];
            cch[2] = F2203.CH[2];

//#if FM_LFO_SUPPORT
	/* LFO */
            lfo_amd = lfo_pmd = 0;
//#endif
	/* frequency counter channel A */
            CALC_FCOUNT(cch[0]);
            /* frequency counter channel B */
            CALC_FCOUNT(cch[1]);
            /* frequency counter channel C */
            if (((State.mode & 0xc0) != 0)) {
                /* 3SLOT MODE */
                if (cch[2].SLOT[SLOT1].Incr == -1) {
                    /* 3 slot mode */
                    CALC_FCSLOT(cch[2].SLOT[SLOT1], (int) OPN.SL3.fc[1], OPN.SL3.kcode[1]);
                    CALC_FCSLOT(cch[2].SLOT[SLOT2], (int) OPN.SL3.fc[2], OPN.SL3.kcode[2]);
                    CALC_FCSLOT(cch[2].SLOT[SLOT3], (int) OPN.SL3.fc[0], OPN.SL3.kcode[0]);
                    CALC_FCSLOT(cch[2].SLOT[SLOT4], (int) cch[2].fc, cch[2].kcode);
                }
            } else {
                CALC_FCOUNT(cch[2]);
            }

            for (i = 0; i < length; i++) {
                /*            channel A         channel B         channel C      */
                out_ch[OUTD_CENTER] = 0;
                /* calcrate FM */
                //for( ch=cch[0] ; ch <= cch[2] ; ch++)
                for (int kk = 0; kk < 2; kk++) {
                    FM_CALC_CH(cch[kk]);
                }
                /* limit check */
                //Limit( out_ch[OUTD_CENTER] , FM_MAXOUT, FM_MINOUT );
                if (out_ch[OUTD_CENTER] > FM_MAXOUT) {
                    out_ch[OUTD_CENTER] = FM_MAXOUT;
                } else if (out_ch[OUTD_CENTER] < FM_MINOUT) {
                    out_ch[OUTD_CENTER] = FM_MINOUT;
                }
                /* store to sound buffer */

                buf.write(i, (short) (out_ch[OUTD_CENTER] >> FM_OUTSB));
                /* timer controll */
                INTERNAL_TIMER_A(State, cch[2]);
            }
            INTERNAL_TIMER_B(State, length);
        }
    };

    /* ---------- reset one of chip ---------- */
    static void YM2203ResetChip(int num) {
        int i;
        FM_OPN OPN = (FM2203[num].OPN);

        /* Reset Priscaler */
        OPNSetPris(OPN, 6 * 12, 6 * 12, 4); /* 1/6 , 1/4 */
        /* reset SSG section */

        SSGReset(OPN.ST.index);
        /* status clear */
        FM_IRQMASK_SET(OPN.ST, 0x03);
        OPNWriteMode(OPN, 0x27, 0x30); /* mode 0 , timer reset */

        reset_channel(OPN.ST, FM2203[num].CH, 3);
        /* reset OPerator paramater */
        for (i = 0xb6; i >= 0xb4; i--) {
            OPNWriteReg(OPN, i, 0xc0); /* PAN RESET */

        }
        for (i = 0xb2; i >= 0x30; i--) {
            OPNWriteReg(OPN, i, 0);
        }
        for (i = 0x26; i >= 0x20; i--) {
            OPNWriteReg(OPN, i, 0);
        }
    }
    /*TODO*///
/*TODO*////* ----------  Initialize YM2203 emulator(s) ----------    */
/*TODO*////* 'num' is the number of virtual YM2203's to allocate     */
/*TODO*////* 'rate' is sampling rate and 'bufsiz' is the size of the */
/*TODO*////* buffer that should be updated at each interval          */

    public static int YM2203Init(int num, int clock, int rate, FM_TIMERHANDLERtr TimerHandler, FM_IRQHANDLEPtr IRQHandler) {
        int i;
        if (FM2203 != null) {
            return (-1);	/* duplicate init. */

        }
        cur_chip = null;	/* hiro-shi!! */

        YM2203NumChips = num;
        FM2203 = new YM2203[YM2203NumChips];
        for (i = 0; i < YM2203NumChips; i++) {
            FM2203[i] = new YM2203();
        }

        /* allocate total level table (128kb space) */
        if (FMInitTable() == 0) {
            FM2203 = null;
            return (-1);
        }
        for (i = 0; i < YM2203NumChips; i++) {
            FM2203[i].OPN.ST.index = i;
            FM2203[i].OPN.type = TYPE_YM2203;
            FM2203[i].OPN.P_CH = FM2203[i].CH;
            FM2203[i].OPN.ST.clock = clock;
            FM2203[i].OPN.ST.rate = rate;
            /* FM2203[i].OPN.ST.irq = 0; */
            /* FM2203[i].OPN.ST.satus = 0; */
            FM2203[i].OPN.ST.timermodel = FM_TIMER_INTERVAL;
            /* Extend handler */
            FM2203[i].OPN.ST.Timer_Handler = TimerHandler;
            FM2203[i].OPN.ST.IRQ_Handler = IRQHandler;
            YM2203ResetChip(i);
        }
        return (0);

    }

    /* ---------- shut down emurator ----------- */
    public static void YM2203Shutdown() {
        if (FM2203 == null) {
            return;
        }

        FMCloseTable();
        FM2203 = null;
    }

    /* ---------- YM2203 I/O interface ---------- */
    public static int YM2203Write(int n, int a, int/*UINT8*/ v) {
        FM_OPN OPN = (FM2203[n].OPN);

        if ((a & 1) == 0) {	/* address port */

            OPN.ST.address = v & 0xff;
            /* Write register to SSG emurator */
            if (v < 16) {
                SSGWrite(n, 0, v);
            }
            switch (OPN.ST.address) {
                case 0x2d:	/* divider sel */

                    OPNSetPris(OPN, 6 * 12, 6 * 12, 4); /* OPN 1/6 , SSG 1/4 */

                    break;
                case 0x2e:	/* divider sel */

                    OPNSetPris(OPN, 3 * 12, 3 * 12, 2); /* OPN 1/3 , SSG 1/2 */

                    break;
                case 0x2f:	/* divider sel */

                    OPNSetPris(OPN, 2 * 12, 2 * 12, 1); /* OPN 1/2 , SSG 1/1 */

                    break;
            }
        } else {	/* data port */

            int addr = OPN.ST.address;
            switch (addr & 0xf0) {
                case 0x00:	/* 0x00-0x0f : SSG section */
                    /* Write data to SSG emurator */

                    SSGWrite(n, a, v);
                    break;
                case 0x20:	/* 0x20-0x2f : Mode section */

                    YM2203UpdateRequest(n);
                    /* write register */
                    OPNWriteMode(OPN, addr, v);
                    break;
                default:	/* 0x30-0xff : OPN section */

                    YM2203UpdateRequest(n);
                    /* write register */
                    OPNWriteReg(OPN, addr, v);
            }
        }
        return OPN.ST.irq;
    }

    public static int/*UINT8*/ YM2203Read(int n, int a) {
        YM2203 F2203 = (FM2203[n]);
        int addr = F2203.OPN.ST.address;
        int ret = 0;

        if ((a & 1) == 0) {	/* status port */

            ret = F2203.OPN.ST.status;
        } else {	/* data port (ONLY SSG) */

            if (addr < 16) {
                ret = SSGRead(n);
            }
        }
        return ret;
    }

    public static int YM2203TimerOver(int n, int c) {

        YM2203 F2203 = (FM2203[n]);

        if (c != 0) {	/* Timer B */

            TimerBOver((F2203.OPN.ST));
        } else {	/* Timer A */

            YM2203UpdateRequest(n);
            /* timer update */
            TimerAOver((F2203.OPN.ST));
            /* CSM mode key,TL controll */
            if ((F2203.OPN.ST.mode & 0x80) != 0) {	/* CSM mode total level latch and auto key on */

                CSMKeyControll((F2203.CH[2]));
            }
        }
        return F2203.OPN.ST.irq;
    }
    /*TODO*///
/*TODO*///#endif /* BUILD_YM2203 */
/*TODO*///
/*TODO*///#if (BUILD_YM2608||BUILD_OPNB)
/*TODO*////* adpcm type A struct */

/*TODO*///
/*TODO*////* here's the virtual YM2610 */

    /*TODO*///
/*TODO*////* here's the virtual YM2608 */
/*TODO*///typedef YM2610 YM2608;
/*TODO*///#endif /* (BUILD_YM2608||BUILD_OPNB) */
/*TODO*///
/*TODO*///#if BUILD_FM_ADPCMA
/*TODO*////***************************************************************/
/*TODO*////*    ADPCMA units are made by Hiromitsu Shioya (MMSND)         */
/*TODO*////***************************************************************/
/*TODO*///
/*TODO*////**** YM2610 ADPCM defines ****/
public static final int ADPCMA_MIXING_LEVEL  =(3); /* ADPCMA mixing level   */
public static final int ADPCM_SHIFT    =(16);      /* frequency step rate   */
public static final int ADPCMA_ADDRESS_SHIFT =8;   /* adpcm A address shift */

public static final int ADPCMA_DECODE_RANGE =2048;
public static final int ADPCMA_DECODE_MIN =(-(ADPCMA_DECODE_RANGE*ADPCMA_MIXING_LEVEL));
public static final int ADPCMA_DECODE_MAX =((ADPCMA_DECODE_RANGE*ADPCMA_MIXING_LEVEL)-1);
public static final int ADPCMA_VOLUME_DIV =1;

static UBytePtr pcmbufA;
static int pcmsizeA;

/************************************************************/
/************************************************************/
/* --------------------- subroutines  --------------------- */
/************************************************************/
/************************************************************/
/************************/
/*    ADPCM A tables    */
/************************/
static int[] jedi_table=new int[(48+1)*16];
static int decode_tableA1[] = {
  -1*16, -1*16, -1*16, -1*16, 2*16, 5*16, 7*16, 9*16,
  -1*16, -1*16, -1*16, -1*16, 2*16, 5*16, 7*16, 9*16
};

/* 0.9 , 0.9 , 0.9 , 0.9 , 1.2 , 1.6 , 2.0 , 2.4 */
/* 8 = -1 , 2 5 8 11 */
/* 9 = -1 , 2 5 9 13 */
/* 10= -1 , 2 6 10 14 */
/* 12= -1 , 2 7 12 17 */
/* 20= -2 , 4 12 20 32 */

static void InitOPNB_ADPCMATable(){
	int step, nib;

	for (step = 0; step <= 48; step++)
	{
		int stepval = (int)Math.floor (16.0 * Math.pow ((double)11.0 / (double)10.0, (double)step) * ADPCMA_MIXING_LEVEL);
		/* loop over all nibbles and compute the difference */
		for (nib = 0; nib < 16; nib++)
		{
			int value = stepval*((nib&0x07)*2+1)/8;
			jedi_table[step*16+nib] = (nib&0x08)!=0 ? -value : value;
		}
	}
}

/**** ADPCM A (Non control type) ****/
public static void OPNB_ADPCM_CALC_CHA( YM2610 F2610, ADPCM_CH ch )
{
	int/*UINT32*/ step;
	int data;

	ch.now_step += ch.step;
	if ( ch.now_step >= (1<<ADPCM_SHIFT) )
	{
		step = ch.now_step >> ADPCM_SHIFT;
		ch.now_step &= (1<<ADPCM_SHIFT)-1;
		/* end check */
		if ( (ch.now_addr+step) > (ch.end<<1) ) {
			ch.flag = 0;
			F2610.adpcm_arrivedEndAddress |= ch.flagMask;
			return;
		}
		do{
			if( (ch.now_addr&1)!=0 ) data = ch.now_data & 0x0f;
			else
			{
				ch.now_data = pcmbufA.read(ch.now_addr>>1);//*(pcmbufA+(ch.now_addr>>1));
				data = (ch.now_data >> 4)&0x0f;
			}
			ch.now_addr++;

			ch.adpcmx += jedi_table[ch.adpcmd+data];
			//Limit( ch.adpcmx,ADPCMA_DECODE_MAX, ADPCMA_DECODE_MIN );
                        if ( ch.adpcmx > ADPCMA_DECODE_MAX )      ch.adpcmx = ADPCMA_DECODE_MAX; 
                    	else if ( ch.adpcmx < ADPCMA_DECODE_MIN ) ch.adpcmx = ADPCMA_DECODE_MIN;
			ch.adpcmd += decode_tableA1[data];
			//Limit( ch.adpcmd, 48*16, 0*16 );
                        if ( ch.adpcmd > 48*16 )      ch.adpcmd = 48*16; 
                    	else if ( ch.adpcmd < 0*16 ) ch.adpcmd = 0*16;
			/**** calc pcm * volume data ****/
			ch.adpcml = ch.adpcmx * ch.volume;
		}while(--step!=0);
	}
	/* output for work of output channels (out_ch[OPNxxxx])*/
	//*(ch.pan) += ch.adpcml;
        ch.pan.write(ch.pan.read()+ ch.adpcml);
}

/* ADPCM type A */
static void FM_ADPCMAWrite(YM2610 F2610,int r,int v)
{
	ADPCM_CH[] adpcm = F2610.adpcm;
	/*UINT8*/int c = r&0x07;

	F2610.adpcmreg[r] = v&0xff; /* stock data */
	switch( r ){
	case 0x00: /* DM,--,C5,C4,C3,C2,C1,C0 */
	  /* F2610->port1state = v&0xff; */
	  if( (v&0x80)==0 ){
	    /* KEY ON */
	    for( c = 0; c < 6; c++ ){
	      if( ((1<<c)&v)!=0 ){
		/**** start adpcm ****/
		adpcm[c].step     = (int)((float)(1<<ADPCM_SHIFT)*((float)F2610.OPN.ST.freqbase)/3.0);
		adpcm[c].now_addr = adpcm[c].start<<1;
		adpcm[c].now_step = (1<<ADPCM_SHIFT)-adpcm[c].step;
		/*adpcm[c].adpcmm   = 0;*/
		adpcm[c].adpcmx   = 0;
		adpcm[c].adpcmd   = 0;
		adpcm[c].adpcml   = 0;
		adpcm[c].flag     = 1;
		if(F2610.pcmbuf==null){			// Check ROM Mapped
		  //Log(LOG_WAR,"YM2610: Attempting to play regular adpcm but no rom is mapped\n");
		  adpcm[c].flag = 0;
		} else{
		  if(adpcm[c].end >= F2610.pcm_size){		// Check End in Range
		    adpcm[c].end = F2610.pcm_size-1;
		  }
		  if(adpcm[c].start >= F2610.pcm_size){	// Check Start in Range
                      adpcm[c].flag = 0;
		  }

                    //Log(LOG_WAR,"YM2610: Start %06X : %02X %02X %02X\n",adpcm[c].start,
                    //pcmbufA[adpcm[c].start],pcmbufA[adpcm[c].start+1],pcmbufA[adpcm[c].start+2]);

		}
		/*** (1<<c)&v ***/
	      }
	      /**** for loop ****/
	    }
	  } else{
	    /* KEY OFF */
	    for( c = 0; c < 6; c++ ){
	      if( ((1<<c)&v)!=0 )  adpcm[c].flag = 0;
	    }
	  }
	  break;
	case 0x01:	/* B0-5 = TL 0.75dB step */
		F2610.adpcmTL = new IntSubArray(TL_TABLE,((v&0x3f)^0x3f)*(int)(0.75/EG_STEP));
		for( c = 0; c < 6; c++ ){
		  adpcm[c].volume = F2610.adpcmTL.read(adpcm[c].IL*(int)(0.75/EG_STEP)) / ADPCMA_DECODE_RANGE / ADPCMA_VOLUME_DIV;
		  /**** calc pcm * volume data ****/
		  adpcm[c].adpcml = adpcm[c].adpcmx * adpcm[c].volume;
		}
		break;
	default:
		c = r&0x07;
		if( c >= 0x06 ) return;
		switch( r&0x38 ){
		case 0x08:	/* B7=L,B6=R,B4-0=IL */
			adpcm[c].IL = (v&0x1f)^0x1f;
			adpcm[c].volume = F2610.adpcmTL.read(adpcm[c].IL*(int)(0.75/EG_STEP)) / ADPCMA_DECODE_RANGE / ADPCMA_VOLUME_DIV;
			//adpcm[c].pan    = &out_ch[(v>>6)&0x03];
                        adpcm[c].pan    = new IntSubArray(out_ch,(v>>6)&0x03);
			/**** calc pcm * volume data ****/
			adpcm[c].adpcml = adpcm[c].adpcmx * adpcm[c].volume;
			break;
		case 0x10:
		case 0x18:
			adpcm[c].start  = ( (F2610.adpcmreg[0x18 + c]*0x0100 | F2610.adpcmreg[0x10 + c]) << ADPCMA_ADDRESS_SHIFT);
			break;
		case 0x20:
		case 0x28:
			adpcm[c].end    = ( (F2610.adpcmreg[0x28 + c]*0x0100 | F2610.adpcmreg[0x20 + c]) << ADPCMA_ADDRESS_SHIFT);
			adpcm[c].end   += (1<<ADPCMA_ADDRESS_SHIFT) - 1;
			break;
		}
	}
}

/*TODO*///#endif /* BUILD_FM_ADPCMA */
/*TODO*///
/*TODO*///#if BUILD_YM2608
/*TODO*////*******************************************************************************/
/*TODO*////*		YM2608 local section                                                   */
/*TODO*////*******************************************************************************/
/*TODO*///static YM2608 *FM2608=NULL;	/* array of YM2608's */
/*TODO*///static int YM2608NumChips;	/* total chip */
/*TODO*///
/*TODO*////* YM2608 Rhythm Number */
/*TODO*///#define RY_BD  0
/*TODO*///#define RY_SD  1
/*TODO*///#define RY_TOP 2
/*TODO*///#define RY_HH  3
/*TODO*///#define RY_TOM 4
/*TODO*///#define RY_RIM 5
/*TODO*///
/*TODO*///#if 0
/*TODO*////* Get next pcm data */
/*TODO*///INLINE int YM2608ReadADPCM(int n)
/*TODO*///{
/*TODO*///	YM2608 *F2608 = &(FM2608[n]);
/*TODO*///	if( F2608->ADMode & 0x20 )
/*TODO*///	{	/* buffer memory */
/*TODO*///		/* F2203->OPN.ST.status |= 0x04; */
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* from PCM data register */
/*TODO*///		FM_STATUS_SET(F2608->OPN.ST,0x08); /* BRDY = 1 */
/*TODO*///		return F2608->ADData;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* Put decoded data */
/*TODO*///INLINE void YM2608WriteADPCM(int n,int v)
/*TODO*///{
/*TODO*///	YM2608 *F2608 = &(FM2608[n]);
/*TODO*///	if( F2608->ADMode & 0x20 )
/*TODO*///	{	/* for buffer */
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* for PCM data port */
/*TODO*///		F2608->ADData = v;
/*TODO*///		FM_STATUS_SET(F2608->OPN.ST,0x08) /* BRDY = 1 */
/*TODO*///	}
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*////* ---------- IRQ flag Controll Write 0x110 ---------- */
/*TODO*///INLINE void YM2608IRQFlagWrite(FM_ST *ST,int n,int v)
/*TODO*///{
/*TODO*///	if( v & 0x80 )
/*TODO*///	{	/* Reset IRQ flag */
/*TODO*///		FM_STATUS_RESET(ST,0xff);
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* Set IRQ mask */
/*TODO*///		/* !!!!!!!!!! pending !!!!!!!!!! */
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///#ifdef YM2608_RHYTHM_PCM
/*TODO*////**** RYTHM (PCM) ****/
/*TODO*///INLINE void YM2608_RYTHM( YM2608 *F2608, ADPCM_CH *ch )
/*TODO*///
/*TODO*///{
/*TODO*///	UINT32 step;
/*TODO*///
/*TODO*///	ch->now_step += ch->step;
/*TODO*///	if ( ch->now_step >= (1<<ADPCM_SHIFT) )
/*TODO*///	{
/*TODO*///		step = ch->now_step >> ADPCM_SHIFT;
/*TODO*///		ch->now_step &= (1<<ADPCM_SHIFT)-1;
/*TODO*///		/* end check */
/*TODO*///		if ( (ch->now_addr+step) > (ch->end<<1) ) {
/*TODO*///			ch->flag = 0;
/*TODO*///			F2608->adpcm_arrivedEndAddress |= ch->flagMask;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		do{
/*TODO*///			/* get a next pcm data */
/*TODO*///			ch->adpcmx = ((short *)pcmbufA)[ch->now_addr];
/*TODO*///			ch->now_addr++;
/*TODO*///			/**** calc pcm * volume data ****/
/*TODO*///			ch->adpcml = ch->adpcmx * ch->volume;
/*TODO*///		}while(--step);
/*TODO*///	}
/*TODO*///	/* output for work of output channels (out_ch[OPNxxxx])*/
/*TODO*///	*(ch->pan) += ch->adpcml;
/*TODO*///}
/*TODO*///#endif /* YM2608_RHYTHM_PCM */
/*TODO*///
/*TODO*////* ---------- update one of chip ----------- */
/*TODO*///void YM2608UpdateOne(int num, INT16 **buffer, int length)
/*TODO*///{
/*TODO*///	YM2608 *F2608 = &(FM2608[num]);
/*TODO*///	FM_OPN *OPN   = &(FM2608[num].OPN);
/*TODO*///	YM_DELTAT *DELTAT = &(F2608[num].deltaT);
/*TODO*///	int i,j;
/*TODO*///	FM_CH *ch;
/*TODO*///	FMSAMPLE  *bufL,*bufR;
/*TODO*///
/*TODO*///	/* setup DELTA-T unit */
/*TODO*///	YM_DELTAT_DECODE_PRESET(DELTAT);
/*TODO*///
/*TODO*///	/* set bufer */
/*TODO*///	bufL = buffer[0];
/*TODO*///	bufR = buffer[1];
/*TODO*///
/*TODO*///	if( (void *)F2608 != cur_chip ){
/*TODO*///		cur_chip = (void *)F2608;
/*TODO*///
/*TODO*///		State = &OPN->ST;
/*TODO*///		cch[0]   = &F2608->CH[0];
/*TODO*///		cch[1]   = &F2608->CH[1];
/*TODO*///		cch[2]   = &F2608->CH[2];
/*TODO*///		cch[3]   = &F2608->CH[3];
/*TODO*///		cch[4]   = &F2608->CH[4];
/*TODO*///		cch[5]   = &F2608->CH[5];
/*TODO*///		/* setup adpcm rom address */
/*TODO*///		pcmbufA  = F2608->pcmbuf;
/*TODO*///		pcmsizeA = F2608->pcm_size;
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		LFOCnt  = OPN->LFOCnt;
/*TODO*///		LFOIncr = OPN->LFOIncr;
/*TODO*///		if( !LFOIncr ) lfo_amd = lfo_pmd = 0;
/*TODO*///		LFO_wave = OPN->LFO_wave;
/*TODO*///#endif
/*TODO*///	}
/*TODO*///	/* update frequency counter */
/*TODO*///	CALC_FCOUNT( cch[0] );
/*TODO*///	CALC_FCOUNT( cch[1] );
/*TODO*///	if( (State->mode & 0xc0) ){
/*TODO*///		/* 3SLOT MODE */
/*TODO*///		if( cch[2]->SLOT[SLOT1].Incr==-1){
/*TODO*///			/* 3 slot mode */
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT1] , OPN->SL3.fc[1] , OPN->SL3.kcode[1] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT2] , OPN->SL3.fc[2] , OPN->SL3.kcode[2] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT3] , OPN->SL3.fc[0] , OPN->SL3.kcode[0] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT4] , cch[2]->fc , cch[2]->kcode );
/*TODO*///		}
/*TODO*///	}else CALC_FCOUNT( cch[2] );
/*TODO*///	CALC_FCOUNT( cch[3] );
/*TODO*///	CALC_FCOUNT( cch[4] );
/*TODO*///	CALC_FCOUNT( cch[5] );
/*TODO*///	/* buffering */
/*TODO*///    for( i=0; i < length ; i++ )
/*TODO*///	{
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		/* LFO */
/*TODO*///		if( LFOIncr )
/*TODO*///		{
/*TODO*///			lfo_amd = LFO_wave[(LFOCnt+=LFOIncr)>>LFO_SHIFT];
/*TODO*///			lfo_pmd = lfo_amd-(LFO_RATE/2);
/*TODO*///		}
/*TODO*///#endif
/*TODO*///		/* clear output acc. */
/*TODO*///		out_ch[OUTD_LEFT] = out_ch[OUTD_RIGHT]= out_ch[OUTD_CENTER] = 0;
/*TODO*///		/**** deltaT ADPCM ****/
/*TODO*///		if( DELTAT->flag )
/*TODO*///			YM_DELTAT_ADPCM_CALC(DELTAT);
/*TODO*///		/* FM */
/*TODO*///		for(ch = cch[0] ; ch <= cch[5] ; ch++)
/*TODO*///			FM_CALC_CH( ch );
/*TODO*///		for( j = 0; j < 6; j++ )
/*TODO*///		{
/*TODO*///			/**** ADPCM ****/
/*TODO*///			if( F2608->adpcm[j].flag )
/*TODO*///#ifdef YM2608_RHYTHM_PCM
/*TODO*///				YM2608_RYTHM(F2608, &F2608->adpcm[j]);
/*TODO*///#else
/*TODO*///				OPNB_ADPCM_CALC_CHA( F2608, &F2608->adpcm[j]);
/*TODO*///#endif
/*TODO*///		}
/*TODO*///		/* buffering */
/*TODO*///		FM_BUFFERING_STEREO;
/*TODO*///		/* timer A controll */
/*TODO*///		INTERNAL_TIMER_A( State , cch[2] )
/*TODO*///	}
/*TODO*///	INTERNAL_TIMER_B(State,length)
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	OPN->LFOCnt = LFOCnt;
/*TODO*///#endif
/*TODO*///}
/*TODO*///
/*TODO*////* -------------------------- YM2608(OPNA) ---------------------------------- */
/*TODO*///int YM2608Init(int num, int clock, int rate,
/*TODO*///               void **pcmrom,int *pcmsize,short *rhythmrom,int *rhythmpos,
/*TODO*///               FM_TIMERHANDLER TimerHandler,FM_IRQHANDLER IRQHandler)
/*TODO*///{
/*TODO*///	int i,j;
/*TODO*///
/*TODO*///    if (FM2608) return (-1);	/* duplicate init. */
/*TODO*///    cur_chip = NULL;	/* hiro-shi!! */
/*TODO*///
/*TODO*///	YM2608NumChips = num;
/*TODO*///
/*TODO*///	/* allocate extend state space */
/*TODO*///	if( (FM2608 = (YM2608 *)malloc(sizeof(YM2608) * YM2608NumChips))==NULL)
/*TODO*///		return (-1);
/*TODO*///	/* clear */
/*TODO*///	memset(FM2608,0,sizeof(YM2608) * YM2608NumChips);
/*TODO*///	/* allocate total level table (128kb space) */
/*TODO*///	if( !FMInitTable() )
/*TODO*///	{
/*TODO*///		free( FM2608 );
/*TODO*///		return (-1);
/*TODO*///	}
/*TODO*///
/*TODO*///	for ( i = 0 ; i < YM2608NumChips; i++ ) {
/*TODO*///		FM2608[i].OPN.ST.index = i;
/*TODO*///		FM2608[i].OPN.type = TYPE_YM2608;
/*TODO*///		FM2608[i].OPN.P_CH = FM2608[i].CH;
/*TODO*///		FM2608[i].OPN.ST.clock = clock;
/*TODO*///		FM2608[i].OPN.ST.rate = rate;
/*TODO*///		/* FM2608[i].OPN.ST.irq = 0; */
/*TODO*///		/* FM2608[i].OPN.ST.status = 0; */
/*TODO*///		FM2608[i].OPN.ST.timermodel = FM_TIMER_INTERVAL;
/*TODO*///		/* Extend handler */
/*TODO*///		FM2608[i].OPN.ST.Timer_Handler = TimerHandler;
/*TODO*///		FM2608[i].OPN.ST.IRQ_Handler   = IRQHandler;
/*TODO*///		/* DELTA-T */
/*TODO*///		FM2608[i].deltaT.memory = (UINT8 *)(pcmrom[i]);
/*TODO*///		FM2608[i].deltaT.memory_size = pcmsize[i];
/*TODO*///		/* ADPCM(Rythm) */
/*TODO*///		FM2608[i].pcmbuf   = (UINT8 *)rhythmrom;
/*TODO*///#ifdef YM2608_RHYTHM_PCM
/*TODO*///		/* rhythm sound setup (PCM) */
/*TODO*///		for(j=0;j<6;j++)
/*TODO*///		{
/*TODO*///			/* rhythm sound */
/*TODO*///			FM2608[i].adpcm[j].start = rhythmpos[j];
/*TODO*///			FM2608[i].adpcm[j].end   = rhythmpos[j+1]-1;
/*TODO*///		}
/*TODO*///		FM2608[i].pcm_size = rhythmpos[6];
/*TODO*///#else
/*TODO*///		/* rhythm sound setup (ADPCM) */
/*TODO*///		FM2608[i].pcm_size = rhythmsize;
/*TODO*///#endif
/*TODO*///		YM2608ResetChip(i);
/*TODO*///	}
/*TODO*///	InitOPNB_ADPCMATable();
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- shut down emurator ----------- */
/*TODO*///void YM2608Shutdown()
/*TODO*///{
/*TODO*///    if (!FM2608) return;
/*TODO*///
/*TODO*///	FMCloseTable();
/*TODO*///	free(FM2608);
/*TODO*///	FM2608 = NULL;
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- reset one of chip ---------- */
/*TODO*///void YM2608ResetChip(int num)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	YM2608 *F2608 = &(FM2608[num]);
/*TODO*///	FM_OPN *OPN   = &(FM2608[num].OPN);
/*TODO*///	YM_DELTAT *DELTAT = &(F2608[num].deltaT);
/*TODO*///
/*TODO*///	/* Reset Priscaler */
/*TODO*///	OPNSetPris( OPN, 6*24, 6*24,4*2); /* OPN 1/6 , SSG 1/4 */
/*TODO*///	/* reset SSG section */
/*TODO*///	SSGReset(OPN->ST.index);
/*TODO*///	/* status clear */
/*TODO*///	FM_IRQMASK_SET(&OPN->ST,0x1f);
/*TODO*///	OPNWriteMode(OPN,0x27,0x30); /* mode 0 , timer reset */
/*TODO*///
/*TODO*///	/* extend 3ch. disable */
/*TODO*///	//OPN->type &= (~TYPE_6CH);
/*TODO*///
/*TODO*///	reset_channel( &OPN->ST , F2608->CH , 6 );
/*TODO*///	/* reset OPerator paramater */
/*TODO*///	for(i = 0xb6 ; i >= 0xb4 ; i-- )
/*TODO*///	{
/*TODO*///		OPNWriteReg(OPN,i      ,0xc0);
/*TODO*///		OPNWriteReg(OPN,i|0x100,0xc0);
/*TODO*///	}
/*TODO*///	for(i = 0xb2 ; i >= 0x30 ; i-- )
/*TODO*///	{
/*TODO*///		OPNWriteReg(OPN,i      ,0);
/*TODO*///		OPNWriteReg(OPN,i|0x100,0);
/*TODO*///	}
/*TODO*///	for(i = 0x26 ; i >= 0x20 ; i-- ) OPNWriteReg(OPN,i,0);
/*TODO*///	/* reset ADPCM unit */
/*TODO*///	/**** ADPCM work initial ****/
/*TODO*///	for( i = 0; i < 6+1; i++ ){
/*TODO*///		F2608->adpcm[i].now_addr  = 0;
/*TODO*///		F2608->adpcm[i].now_step  = 0;
/*TODO*///		F2608->adpcm[i].step      = 0;
/*TODO*///		F2608->adpcm[i].start     = 0;
/*TODO*///		F2608->adpcm[i].end       = 0;
/*TODO*///		/* F2608->adpcm[i].delta     = 21866; */
/*TODO*///		F2608->adpcm[i].volume    = 0;
/*TODO*///		F2608->adpcm[i].pan       = &out_ch[OUTD_CENTER]; /* default center */
/*TODO*///		F2608->adpcm[i].flagMask  = (i == 6) ? 0x20 : 0;
/*TODO*///		F2608->adpcm[i].flag      = 0;
/*TODO*///		F2608->adpcm[i].adpcmx    = 0;
/*TODO*///		F2608->adpcm[i].adpcmd    = 127;
/*TODO*///		F2608->adpcm[i].adpcml    = 0;
/*TODO*///	}
/*TODO*///	F2608->adpcmTL = &(TL_TABLE[0x3f*(int)(0.75/EG_STEP)]);
/*TODO*///	/* F2608->port1state = -1; */
/*TODO*///	F2608->adpcm_arrivedEndAddress = 0; /* don't used */
/*TODO*///
/*TODO*///	/* DELTA-T unit */
/*TODO*///	DELTAT->freqbase = OPN->ST.freqbase;
/*TODO*///	DELTAT->output_pointer = out_ch;
/*TODO*///	DELTAT->portshift = 8;		/* allways 8bits shift */
/*TODO*///	DELTAT->output_range = DELTAT_MIXING_LEVEL<<TL_BITS;
/*TODO*///	YM_DELTAT_ADPCM_Reset(DELTAT,OUTD_CENTER);
/*TODO*///}
/*TODO*///
/*TODO*////* YM2608 write */
/*TODO*////* n = number  */
/*TODO*////* a = address */
/*TODO*////* v = value   */
/*TODO*///int YM2608Write(int n, int a,UINT8 v)
/*TODO*///{
/*TODO*///	YM2608 *F2608 = &(FM2608[n]);
/*TODO*///	FM_OPN *OPN   = &(FM2608[n].OPN);
/*TODO*///	int addr;
/*TODO*///
/*TODO*///	switch(a&3){
/*TODO*///	case 0:	/* address port 0 */
/*TODO*///		OPN->ST.address = v & 0xff;
/*TODO*///		/* Write register to SSG emurator */
/*TODO*///		if( v < 16 ) SSGWrite(n,0,v);
/*TODO*///		switch(OPN->ST.address)
/*TODO*///		{
/*TODO*///		case 0x2d:	/* divider sel */
/*TODO*///			OPNSetPris( OPN, 6*24, 6*24, 4*2); /* OPN 1/6 , SSG 1/4 */
/*TODO*///			F2608->deltaT.freqbase = OPN->ST.freqbase;
/*TODO*///			break;
/*TODO*///		case 0x2e:	/* divider sel */
/*TODO*///			OPNSetPris( OPN, 3*24, 3*24,2*2); /* OPN 1/3 , SSG 1/2 */
/*TODO*///			F2608->deltaT.freqbase = OPN->ST.freqbase;
/*TODO*///			break;
/*TODO*///		case 0x2f:	/* divider sel */
/*TODO*///			OPNSetPris( OPN, 2*24, 2*24,1*2); /* OPN 1/2 , SSG 1/1 */
/*TODO*///			F2608->deltaT.freqbase = OPN->ST.freqbase;
/*TODO*///			break;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	case 1:	/* data port 0    */
/*TODO*///		addr = OPN->ST.address;
/*TODO*///		switch(addr & 0xf0)
/*TODO*///		{
/*TODO*///		case 0x00:	/* SSG section */
/*TODO*///			/* Write data to SSG emurator */
/*TODO*///			SSGWrite(n,a,v);
/*TODO*///			break;
/*TODO*///		case 0x10:	/* 0x10-0x1f : Rhythm section */
/*TODO*///			YM2608UpdateReq(n);
/*TODO*///			FM_ADPCMAWrite(F2608,addr-0x10,v);
/*TODO*///			break;
/*TODO*///		case 0x20:	/* Mode Register */
/*TODO*///			switch(addr)
/*TODO*///			{
/*TODO*///			case 0x29: /* SCH,xirq mask */
/*TODO*///				/* SCH,xx,xxx,EN_ZERO,EN_BRDY,EN_EOS,EN_TB,EN_TA */
/*TODO*///				/* extend 3ch. enable/disable */
/*TODO*///				if(v&0x80) OPN->type |= TYPE_6CH;
/*TODO*///				else       OPN->type &= ~TYPE_6CH;
/*TODO*///				/* IRQ MASK */
/*TODO*///				FM_IRQMASK_SET(&OPN->ST,v&0x1f);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				YM2608UpdateReq(n);
/*TODO*///				OPNWriteMode(OPN,addr,v);
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		default:	/* OPN section */
/*TODO*///			YM2608UpdateReq(n);
/*TODO*///			OPNWriteReg(OPN,addr,v);
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	case 2:	/* address port 1 */
/*TODO*///		F2608->address1 = v & 0xff;
/*TODO*///		break;
/*TODO*///	case 3:	/* data port 1    */
/*TODO*///		addr = F2608->address1;
/*TODO*///		YM2608UpdateReq(n);
/*TODO*///		switch( addr & 0xf0 )
/*TODO*///		{
/*TODO*///		case 0x00:	/* ADPCM PORT */
/*TODO*///			switch( addr )
/*TODO*///			{
/*TODO*///			case 0x0c:	/* Limit address L */
/*TODO*///				//F2608->ADLimit = (F2608->ADLimit & 0xff00) | v;
/*TODO*///				//break;
/*TODO*///			case 0x0d:	/* Limit address H */
/*TODO*///				//F2608->ADLimit = (F2608->ADLimit & 0x00ff) | (v<<8);
/*TODO*///				//break;
/*TODO*///			case 0x0e:	/* DAC data */
/*TODO*///				//break;
/*TODO*///			case 0x0f:	/* PCM data port */
/*TODO*///				//F2608->ADData = v;
/*TODO*///				//FM_STATUS_RESET(F2608->OPN.ST,0x08);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				/* 0x00-0x0b */
/*TODO*///				YM_DELTAT_ADPCM_Write(&F2608->deltaT,addr,v);
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 0x10:	/* IRQ Flag controll */
/*TODO*///			if( addr == 0x10 )
/*TODO*///				YM2608IRQFlagWrite(&(OPN->ST),n,v);
/*TODO*///			break;
/*TODO*///		default:
/*TODO*///			OPNWriteReg(OPN,addr|0x100,v);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return OPN->ST.irq;
/*TODO*///}
/*TODO*///UINT8 YM2608Read(int n,int a)
/*TODO*///{
/*TODO*///	YM2608 *F2608 = &(FM2608[n]);
/*TODO*///	int addr = F2608->OPN.ST.address;
/*TODO*///	int ret = 0;
/*TODO*///
/*TODO*///	switch( a&3 ){
/*TODO*///	case 0:	/* status 0 : YM2203 compatible */
/*TODO*///		/* BUSY:x:x:x:x:x:FLAGB:FLAGA */
/*TODO*///		if(addr==0xff) ret = 0x00; /* ID code */
/*TODO*///		else ret = F2608->OPN.ST.status & 0x83;
/*TODO*///		break;
/*TODO*///	case 1:	/* status 0 */
/*TODO*///		if( addr < 16 ) ret = SSGRead(n);
/*TODO*///		break;
/*TODO*///	case 2:	/* status 1 : + ADPCM status */
/*TODO*///		/* BUSY:x:PCMBUSY:ZERO:BRDY:EOS:FLAGB:FLAGA */
/*TODO*///		if(addr==0xff) ret = 0x00; /* ID code */
/*TODO*///		else ret = F2608->OPN.ST.status | (F2608->adpcm[6].flag ? 0x20 : 0);
/*TODO*///		break;
/*TODO*///	case 3:
/*TODO*///		ret = 0;
/*TODO*///		break;
/*TODO*///	}
/*TODO*///	return ret;
/*TODO*///}
/*TODO*///
/*TODO*///int YM2608TimerOver(int n,int c)
/*TODO*///{
/*TODO*///	YM2608 *F2608 = &(FM2608[n]);
/*TODO*///
/*TODO*///	if( c )
/*TODO*///	{	/* Timer B */
/*TODO*///		TimerBOver( &(F2608->OPN.ST) );
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* Timer A */
/*TODO*///		YM2608UpdateReq(n);
/*TODO*///		/* timer update */
/*TODO*///		TimerAOver( &(F2608->OPN.ST) );
/*TODO*///		/* CSM mode key,TL controll */
/*TODO*///		if( F2608->OPN.ST.mode & 0x80 )
/*TODO*///		{	/* CSM mode total level latch and auto key on */
/*TODO*///			CSMKeyControll( &(F2608->CH[2]) );
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return FM2608->OPN.ST.irq;
/*TODO*///}
/*TODO*///
/*TODO*///#endif /* BUILD_YM2608 */
/*TODO*///
/*TODO*///#if BUILD_OPNB
/* -------------------------- YM2610(OPNB) ---------------------------------- */
    static YM2610[] FM2610 = null;	/* array of YM2610's */

    static int YM2610NumChips;	/* total chip */

    /* ---------- update one of chip (YM2610B FM6: ADPCM-A6: ADPCM-B:1) ----------- */
    public static StreamInitMultiPtr YM2610UpdateOne = new StreamInitMultiPtr() {
        public void handler(int num, ShortPtr[] buffer, int length) {

            YM2610 F2610 = FM2610[num];
            FM_OPN OPN = FM2610[num].OPN;
            YM_DELTAT DELTAT = FM2610[num].deltaT;
            int i, j;
            int ch;
            ShortPtr bufL;
            ShortPtr bufR;

            /* setup DELTA-T unit */
            YM_DELTAT_DECODE_PRESET(DELTAT);

            /* buffer setup */
            bufL = buffer[0];
            bufR = buffer[1];

            if (F2610 != cur_chip) {
                cur_chip = F2610;
                State = OPN.ST;
                cch[0] = F2610.CH[1];
                cch[1] = F2610.CH[2];
                cch[2] = F2610.CH[4];
                cch[3] = F2610.CH[5];
                /*TODO*///		/* setup adpcm rom address */
		pcmbufA  = F2610.pcmbuf;
		pcmsizeA = F2610.pcm_size;
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		LFOCnt  = OPN->LFOCnt;
/*TODO*///		LFOIncr = OPN->LFOIncr;
/*TODO*///		if( !LFOIncr ) lfo_amd = lfo_pmd = 0;
/*TODO*///		LFO_wave = OPN->LFO_wave;
/*TODO*///#endif
            }
            /* update frequency counter */
            CALC_FCOUNT(cch[0]);
            if ((State.mode & 0xc0) != 0) {
                /* 3SLOT MODE */
                if (cch[1].SLOT[SLOT1].Incr == -1) {
                    /* 3 slot mode */
                    CALC_FCSLOT(cch[1].SLOT[SLOT1], (int) OPN.SL3.fc[1], OPN.SL3.kcode[1]);
                    CALC_FCSLOT(cch[1].SLOT[SLOT2], (int) OPN.SL3.fc[2], OPN.SL3.kcode[2]);
                    CALC_FCSLOT(cch[1].SLOT[SLOT3], (int) OPN.SL3.fc[0], OPN.SL3.kcode[0]);
                    CALC_FCSLOT(cch[1].SLOT[SLOT4], (int) cch[1].fc, cch[1].kcode);
                }
            } else {
                CALC_FCOUNT(cch[1]);
            }
            CALC_FCOUNT(cch[2]);
            CALC_FCOUNT(cch[3]);

            /* buffering */
            for (i = 0; i < length; i++) {
                /*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		/* LFO */
/*TODO*///		if( LFOIncr )
/*TODO*///		{
/*TODO*///			lfo_amd = LFO_wave[(LFOCnt+=LFOIncr)>>LFO_SHIFT];
/*TODO*///			lfo_pmd = lfo_amd-(LFO_RATE/2);
/*TODO*///		}
/*TODO*///#endif
		/* clear output acc. */
                out_ch[OUTD_LEFT] = out_ch[OUTD_RIGHT] = out_ch[OUTD_CENTER] = 0;
                /**
                 * ** deltaT ADPCM ***
                 */
                if (DELTAT.flag != 0) {
                    YM_DELTAT_ADPCM_CALC(DELTAT);
                }
                /* FM */
                for (ch = 0; ch < 4; ch++) {
                    FM_CALC_CH(cch[ch]);
                }
                		for( j = 0; j < 6; j++ )
		{
			/**** ADPCM ****/
			if( F2610.adpcm[j].flag!=0 )
				OPNB_ADPCM_CALC_CHA( F2610, F2610.adpcm[j]);
		}
		/* buffering */
                //FM_BUFFERING_STEREO;
                {
                    /* get left & right output with clipping */
                    out_ch[OUTD_LEFT] += out_ch[OUTD_CENTER];
                    //Limit(ref  out_ch[OUTD_LEFT], FM_MAXOUT, FM_MINOUT);
                    out_ch[OUTD_RIGHT] += out_ch[OUTD_CENTER];
                    //Limit(ref  out_ch[OUTD_RIGHT], FM_MAXOUT, FM_MINOUT);
                        /* buffering */
                    bufL.write(i, (short) (out_ch[OUTD_LEFT] >> FM_OUTSB));
                    bufR.write(i, (short) (out_ch[OUTD_RIGHT] >> FM_OUTSB));
                }
                /* timer A controll */
                INTERNAL_TIMER_A(State, cch[1]);
            }
            INTERNAL_TIMER_B(State, length);
            /*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	OPN->LFOCnt = LFOCnt;
/*TODO*///#endif
        }
    };
    /*TODO*///#endif /* BUILD_OPNB */
/*TODO*///
/*TODO*///#if BUILD_YM2610B
/*TODO*////* ---------- update one of chip (YM2610B FM6: ADPCM-A6: ADPCM-B:1) ----------- */
/*TODO*///void YM2610BUpdateOne(int num, INT16 **buffer, int length)
/*TODO*///{
/*TODO*///	YM2610 *F2610 = &(FM2610[num]);
/*TODO*///	FM_OPN *OPN   = &(FM2610[num].OPN);
/*TODO*///	YM_DELTAT *DELTAT = &(FM2610[num].deltaT);
/*TODO*///	int i,j;
/*TODO*///	FM_CH *ch;
/*TODO*///	FMSAMPLE  *bufL,*bufR;
/*TODO*///
/*TODO*///	/* setup DELTA-T unit */
/*TODO*///	YM_DELTAT_DECODE_PRESET(DELTAT);
/*TODO*///	/* buffer setup */
/*TODO*///	bufL = buffer[0];
/*TODO*///	bufR = buffer[1];
/*TODO*///
/*TODO*///	if( (void *)F2610 != cur_chip ){
/*TODO*///		cur_chip = (void *)F2610;
/*TODO*///		State = &OPN->ST;
/*TODO*///		cch[0] = &F2610->CH[0];
/*TODO*///		cch[1] = &F2610->CH[1];
/*TODO*///		cch[2] = &F2610->CH[2];
/*TODO*///		cch[3] = &F2610->CH[3];
/*TODO*///		cch[4] = &F2610->CH[4];
/*TODO*///		cch[5] = &F2610->CH[5];
/*TODO*///		/* setup adpcm rom address */
/*TODO*///		pcmbufA  = F2610->pcmbuf;
/*TODO*///		pcmsizeA = F2610->pcm_size;
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		LFOCnt  = OPN->LFOCnt;
/*TODO*///		LFOIncr = OPN->LFOIncr;
/*TODO*///		if( !LFOIncr ) lfo_amd = lfo_pmd = 0;
/*TODO*///		LFO_wave = OPN->LFO_wave;
/*TODO*///#endif
/*TODO*///	}
/*TODO*///
/*TODO*///	/* update frequency counter */
/*TODO*///	CALC_FCOUNT( cch[0] );
/*TODO*///	CALC_FCOUNT( cch[1] );
/*TODO*///	if( (State->mode & 0xc0) ){
/*TODO*///		/* 3SLOT MODE */
/*TODO*///		if( cch[2]->SLOT[SLOT1].Incr==-1){
/*TODO*///			/* 3 slot mode */
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT1] , OPN->SL3.fc[1] , OPN->SL3.kcode[1] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT2] , OPN->SL3.fc[2] , OPN->SL3.kcode[2] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT3] , OPN->SL3.fc[0] , OPN->SL3.kcode[0] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT4] , cch[2]->fc , cch[2]->kcode );
/*TODO*///		}
/*TODO*///	}else CALC_FCOUNT( cch[2] );
/*TODO*///	CALC_FCOUNT( cch[3] );
/*TODO*///	CALC_FCOUNT( cch[4] );
/*TODO*///	CALC_FCOUNT( cch[5] );
/*TODO*///
/*TODO*///	/* buffering */
/*TODO*///    for( i=0; i < length ; i++ )
/*TODO*///	{
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		/* LFO */
/*TODO*///		if( LFOIncr )
/*TODO*///		{
/*TODO*///			lfo_amd = LFO_wave[(LFOCnt+=LFOIncr)>>LFO_SHIFT];
/*TODO*///			lfo_pmd = lfo_amd-(LFO_RATE/2);
/*TODO*///		}
/*TODO*///#endif
/*TODO*///		/* clear output acc. */
/*TODO*///		out_ch[OUTD_LEFT] = out_ch[OUTD_RIGHT]= out_ch[OUTD_CENTER] = 0;
/*TODO*///		/**** deltaT ADPCM ****/
/*TODO*///		if( DELTAT->flag )
/*TODO*///			YM_DELTAT_ADPCM_CALC(DELTAT);
/*TODO*///		/* FM */
/*TODO*///		for(ch = cch[0] ; ch <= cch[5] ; ch++)
/*TODO*///			FM_CALC_CH( ch );
/*TODO*///		for( j = 0; j < 6; j++ )
/*TODO*///		{
/*TODO*///			/**** ADPCM ****/
/*TODO*///			if( F2610->adpcm[j].flag )
/*TODO*///				OPNB_ADPCM_CALC_CHA( F2610, &F2610->adpcm[j]);
/*TODO*///		}
/*TODO*///		/* buffering */
/*TODO*///		FM_BUFFERING_STEREO;
/*TODO*///		/* timer A controll */
/*TODO*///		INTERNAL_TIMER_A( State , cch[2] )
/*TODO*///	}
/*TODO*///	INTERNAL_TIMER_B(State,length)
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	OPN->LFOCnt = LFOCnt;
/*TODO*///#endif
/*TODO*///}
/*TODO*///#endif /* BUILD_YM2610B */
/*TODO*///
/*TODO*///#if BUILD_OPNB

    public static int YM2610Init(int num, int clock, int rate,
            UBytePtr[] pcmroma, int[] pcmsizea, UBytePtr[] pcmromb, int[] pcmsizeb,
            FM_TIMERHANDLERtr TimerHandler, FM_IRQHANDLEPtr IRQHandler) {
        int i;

        if (FM2610 != null) {
            return (-1);	/* duplicate init. */

        }
        cur_chip = null;	/* hiro-shi!! */

        YM2610NumChips = num;

        FM2610 = new YM2610[YM2610NumChips];
        for (i = 0; i < YM2610NumChips; i++) {
            FM2610[i] = new YM2610();
        }
        if (FMInitTable() == 0) {
            FM2610 = null;
            return (-1);
        }

        for (i = 0; i < YM2610NumChips; i++) {
            /* FM */
            FM2610[i].OPN.ST.index = i;
            FM2610[i].OPN.type = TYPE_YM2610;
            FM2610[i].OPN.P_CH = FM2610[i].CH;
            FM2610[i].OPN.ST.clock = clock;
            FM2610[i].OPN.ST.rate = rate;
            /* FM2610[i].OPN.ST.irq = 0; */
            /* FM2610[i].OPN.ST.status = 0; */
            FM2610[i].OPN.ST.timermodel = FM_TIMER_INTERVAL;
            /* Extend handler */
            FM2610[i].OPN.ST.Timer_Handler = TimerHandler;
            FM2610[i].OPN.ST.IRQ_Handler = IRQHandler;
            /* ADPCM */
            FM2610[i].pcmbuf = pcmroma[i];
            FM2610[i].pcm_size = pcmsizea[i];
            /* DELTA-T */
            FM2610[i].deltaT = new YM_DELTAT();
            FM2610[i].deltaT.memory = pcmromb[i];
            FM2610[i].deltaT.memory_size = pcmsizeb[i];
            /* */
            YM2610ResetChip(i);
        }
        InitOPNB_ADPCMATable();
        return 0;
    }

    /* ---------- shut down emurator ----------- */
    public static void YM2610Shutdown() {
           if (FM2610==null) return;

	FMCloseTable();
	FM2610=null;
    }
    /* ---------- reset one of chip ---------- */

    public static void YM2610ResetChip(int num) {
        int i;
        YM2610 F2610 = FM2610[num];
        FM_OPN OPN = FM2610[num].OPN;
        YM_DELTAT DELTAT = FM2610[num].deltaT;

        /* Reset Priscaler */
        OPNSetPris(OPN, 6 * 24, 6 * 24, 4 * 2); /* OPN 1/6 , SSG 1/4 */
        /* reset SSG section */

        SSGReset(OPN.ST.index);
        /* status clear */
        FM_IRQMASK_SET(OPN.ST, 0x03);
        OPNWriteMode(OPN, 0x27, 0x30); /* mode 0 , timer reset */

        reset_channel(OPN.ST, F2610.CH, 6);
        /* reset OPerator paramater */
        for (i = 0xb6; i >= 0xb4; i--) {
            OPNWriteReg(OPN, i, 0xc0);
            OPNWriteReg(OPN, i | 0x100, 0xc0);
        }
        for (i = 0xb2; i >= 0x30; i--) {
            OPNWriteReg(OPN, i, 0);
            OPNWriteReg(OPN, i | 0x100, 0);
        }
        for (i = 0x26; i >= 0x20; i--) {
            OPNWriteReg(OPN, i, 0);
        }
        	/**** ADPCM work initial ****/
	for( i = 0; i < 6+1; i++ ){
		F2610.adpcm[i].now_addr  = 0;
		F2610.adpcm[i].now_step  = 0;
		F2610.adpcm[i].step      = 0;
		F2610.adpcm[i].start     = 0;
		F2610.adpcm[i].end       = 0;
		/* F2610.adpcm[i].delta     = 21866; */
		F2610.adpcm[i].volume    = 0;
		//F2610.adpcm[i].pan       = &out_ch[OUTD_CENTER]; /* default center */
                F2610.adpcm[i].pan = new IntSubArray(out_ch,OUTD_CENTER);
		F2610.adpcm[i].flagMask  = (i == 6) ? 0x80 : (1<<i);
		F2610.adpcm[i].flag      = 0;
		F2610.adpcm[i].adpcmx    = 0;
		F2610.adpcm[i].adpcmd    = 127;
		F2610.adpcm[i].adpcml    = 0;
	}
	F2610.adpcmTL = new IntSubArray(TL_TABLE,0x3f*(int)(0.75/EG_STEP));
	/* F2610.port1state = -1; */
	F2610.adpcm_arrivedEndAddress = 0;

        /* DELTA-T unit */
        DELTAT.freqbase = OPN.ST.freqbase;
        DELTAT.output_pointer = out_ch;
        DELTAT.portshift = 8;		/* allways 8bits shift */

        DELTAT.output_range = DELTAT_MIXING_LEVEL << TL_BITS;
        YM_DELTAT_ADPCM_Reset(DELTAT, OUTD_CENTER);
    }

    /* YM2610 write */
    /* n = number  */
    /* a = address */
    /* v = value   */
    public static int YM2610Write(int n, int a,/*UINT8*/ int v) {
        YM2610 F2610 = FM2610[n];
        FM_OPN OPN = FM2610[n].OPN;
        int addr;
        int ch;

        switch (a & 3) {
            case 0:	/* address port 0 */

                OPN.ST.address = v & 0xff;
                /* Write register to SSG emurator */
                if (v < 16) {
                    SSGWrite(n, 0, v);
                }
                break;
            case 1:	/* data port 0    */

                addr = OPN.ST.address;
                switch (addr & 0xf0) {
                    case 0x00:	/* SSG section */
                        /* Write data to SSG emurator */

                        SSGWrite(n, a, v);
                        break;
                    case 0x10: /* DeltaT ADPCM */

                        YM2610UpdateRequest(n);
                        switch (addr) {
                            case 0x1c: /*  FLAG CONTROL : Extend Status Clear/Mask */ {
                                int/*UINT8*/ statusmask = (~v) & 0xFF;
                                /* set arrived flag mask */
                                for (ch = 0; ch < 6; ch++) {
                                    /*TODO*///                                    F2610.adpcm[ch].flagMask = statusmask & (1 << ch);
                                }
                                F2610.deltaT.flagMask = statusmask & 0x80;
                                /* clear arrived flag */
                                F2610.adpcm_arrivedEndAddress &= statusmask & 0x3f;
                                F2610.deltaT.arrivedFlag &= F2610.deltaT.flagMask;
                            }
                            break;
                            default:
                                /* 0x10-0x1b */
                                YM_DELTAT_ADPCM_Write(F2610.deltaT, addr - 0x10, v);
                        }
                        break;
                    case 0x20:	/* Mode Register */

                        YM2610UpdateRequest(n);
                        OPNWriteMode(OPN, addr, v);
                        break;
                    default:	/* OPN section */

                        YM2610UpdateRequest(n);
                        /* write register */
                        OPNWriteReg(OPN, addr, v);
                }
                break;
            case 2:	/* address port 1 */

                F2610.address1 = v & 0xff;
                break;
            case 3:	/* data port 1    */

                YM2610UpdateRequest(n);
                addr = F2610.address1;
                if (addr < 0x30) {
                    			/* 100-12f : ADPCM A section */
			FM_ADPCMAWrite(F2610,addr,v);
                } else {
                    OPNWriteReg(OPN, addr | 0x100, v);
                }
        }
        return OPN.ST.irq;
    }

    public static /*UINT8*/ int YM2610Read(int n, int a) {
        YM2610 F2610 = FM2610[n];
        int addr = F2610.OPN.ST.address;
        int/*UINT8*/ ret = 0;

        switch (a & 3) {
            case 0:	/* status 0 : YM2203 compatible */

                ret = F2610.OPN.ST.status & 0x83;
                break;
            case 1:	/* data 0 */

                if (addr < 16) {
                    ret = SSGRead(n);
                }
                if (addr == 0xff) {
                    ret = 0x01;
                }
                break;
            case 2:	/* status 1 : + ADPCM status */
                /* ADPCM STATUS (arrived End Address) */
                /* B,--,A5,A4,A3,A2,A1,A0 */
                /* B     = ADPCM-B(DELTA-T) arrived end address */
                /* A0-A5 = ADPCM-A          arrived end address */

                ret = F2610.adpcm_arrivedEndAddress | F2610.deltaT.arrivedFlag;

                break;
            case 3:
                ret = 0;
                break;
        }
        return ret & 0xFF;
    }

    public static int YM2610TimerOver(int n, int c) {
        YM2610 F2610 = FM2610[n];

        if (c != 0) {	/* Timer B */

            TimerBOver(F2610.OPN.ST);
        } else {	/* Timer A */

            YM2610UpdateRequest(n);
            /* timer update */
            TimerAOver(F2610.OPN.ST);
            /* CSM mode key,TL controll */
            if ((F2610.OPN.ST.mode & 0x80) != 0) {	/* CSM mode total level latch and auto key on */

                CSMKeyControll(F2610.CH[2]);
            }
        }
        return F2610.OPN.ST.irq;
    }
    /*TODO*///
/*TODO*///#endif /* BUILD_OPNB */
/*TODO*///
/*TODO*///
/*TODO*///#if BUILD_YM2612
/*TODO*////*******************************************************************************/
/*TODO*////*		YM2612 local section                                                   */
/*TODO*////*******************************************************************************/
/*TODO*////* here's the virtual YM2612 */
/*TODO*///typedef struct ym2612_f {
/*TODO*///	FM_OPN OPN;						/* OPN state       */
/*TODO*///	FM_CH CH[6];					/* channel state */
/*TODO*///	int address1;	/* address register1 */
/*TODO*///	/* dac output (YM2612) */
/*TODO*///	int dacen;
/*TODO*///	int dacout;
/*TODO*///} YM2612;
/*TODO*///
/*TODO*///static int YM2612NumChips;	/* total chip */
/*TODO*///static YM2612 *FM2612=NULL;	/* array of YM2612's */
/*TODO*///
/*TODO*///static int dacen;
/*TODO*///
/*TODO*////* ---------- update one of chip ----------- */
/*TODO*///void YM2612UpdateOne(int num, INT16 **buffer, int length)
/*TODO*///{
/*TODO*///	YM2612 *F2612 = &(FM2612[num]);
/*TODO*///	FM_OPN *OPN   = &(FM2612[num].OPN);
/*TODO*///	int i;
/*TODO*///	FM_CH *ch,*ech;
/*TODO*///	FMSAMPLE  *bufL,*bufR;
/*TODO*///	int dacout  = F2612->dacout;
/*TODO*///
/*TODO*///	/* set bufer */
/*TODO*///	bufL = buffer[0];
/*TODO*///	bufR = buffer[1];
/*TODO*///
/*TODO*///	if( (void *)F2612 != cur_chip ){
/*TODO*///		cur_chip = (void *)F2612;
/*TODO*///
/*TODO*///		State = &OPN->ST;
/*TODO*///		cch[0]   = &F2612->CH[0];
/*TODO*///		cch[1]   = &F2612->CH[1];
/*TODO*///		cch[2]   = &F2612->CH[2];
/*TODO*///		cch[3]   = &F2612->CH[3];
/*TODO*///		cch[4]   = &F2612->CH[4];
/*TODO*///		cch[5]   = &F2612->CH[5];
/*TODO*///		/* DAC mode */
/*TODO*///		dacen = F2612->dacen;
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		LFOCnt  = OPN->LFOCnt;
/*TODO*///		LFOIncr = OPN->LFOIncr;
/*TODO*///		if( !LFOIncr ) lfo_amd = lfo_pmd = 0;
/*TODO*///		LFO_wave = OPN->LFO_wave;
/*TODO*///#endif
/*TODO*///	}
/*TODO*///	/* update frequency counter */
/*TODO*///	CALC_FCOUNT( cch[0] );
/*TODO*///	CALC_FCOUNT( cch[1] );
/*TODO*///	if( (State->mode & 0xc0) ){
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		/* LFO */
/*TODO*///		if( LFOIncr )
/*TODO*///		{
/*TODO*///			lfo_amd = LFO_wave[(LFOCnt+=LFOIncr)>>LFO_SHIFT];
/*TODO*///			lfo_pmd = lfo_amd-(LFO_RATE/2);
/*TODO*///		}
/*TODO*///#endif
/*TODO*///		/* 3SLOT MODE */
/*TODO*///		if( cch[2]->SLOT[SLOT1].Incr==-1){
/*TODO*///			/* 3 slot mode */
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT1] , OPN->SL3.fc[1] , OPN->SL3.kcode[1] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT2] , OPN->SL3.fc[2] , OPN->SL3.kcode[2] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT3] , OPN->SL3.fc[0] , OPN->SL3.kcode[0] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT4] , cch[2]->fc , cch[2]->kcode );
/*TODO*///		}
/*TODO*///	}else CALC_FCOUNT( cch[2] );
/*TODO*///	CALC_FCOUNT( cch[3] );
/*TODO*///	CALC_FCOUNT( cch[4] );
/*TODO*///	CALC_FCOUNT( cch[5] );
/*TODO*///
/*TODO*///	ech = dacen ? cch[4] : cch[5];
/*TODO*///	/* buffering */
/*TODO*///    for( i=0; i < length ; i++ )
/*TODO*///	{
/*TODO*///		/* clear output acc. */
/*TODO*///		out_ch[OUTD_LEFT] = out_ch[OUTD_RIGHT]= out_ch[OUTD_CENTER] = 0;
/*TODO*///		/* calcrate channel output */
/*TODO*///		for(ch = cch[0] ; ch <= ech ; ch++)
/*TODO*///			FM_CALC_CH( ch );
/*TODO*///		if( dacen )  *cch[5]->connect4 += dacout;
/*TODO*///		/* buffering */
/*TODO*///		FM_BUFFERING_STEREO;
/*TODO*///		/* timer A controll */
/*TODO*///		INTERNAL_TIMER_A( State , cch[2] )
/*TODO*///	}
/*TODO*///	INTERNAL_TIMER_B(State,length)
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	OPN->LFOCnt = LFOCnt;
/*TODO*///#endif
/*TODO*///}
/*TODO*///
/*TODO*////* -------------------------- YM2612 ---------------------------------- */
/*TODO*///int YM2612Init(int num, int clock, int rate,
/*TODO*///               FM_TIMERHANDLER TimerHandler,FM_IRQHANDLER IRQHandler)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///    if (FM2612) return (-1);	/* duplicate init. */
/*TODO*///    cur_chip = NULL;	/* hiro-shi!! */
/*TODO*///
/*TODO*///	YM2612NumChips = num;
/*TODO*///
/*TODO*///	/* allocate extend state space */
/*TODO*///	if( (FM2612 = (YM2612 *)malloc(sizeof(YM2612) * YM2612NumChips))==NULL)
/*TODO*///		return (-1);
/*TODO*///	/* clear */
/*TODO*///	memset(FM2612,0,sizeof(YM2612) * YM2612NumChips);
/*TODO*///	/* allocate total level table (128kb space) */
/*TODO*///	if( !FMInitTable() )
/*TODO*///	{
/*TODO*///		free( FM2612 );
/*TODO*///		return (-1);
/*TODO*///	}
/*TODO*///
/*TODO*///	for ( i = 0 ; i < YM2612NumChips; i++ ) {
/*TODO*///		FM2612[i].OPN.ST.index = i;
/*TODO*///		FM2612[i].OPN.type = TYPE_YM2612;
/*TODO*///		FM2612[i].OPN.P_CH = FM2612[i].CH;
/*TODO*///		FM2612[i].OPN.ST.clock = clock;
/*TODO*///		FM2612[i].OPN.ST.rate = rate;
/*TODO*///		/* FM2612[i].OPN.ST.irq = 0; */
/*TODO*///		/* FM2612[i].OPN.ST.status = 0; */
/*TODO*///		FM2612[i].OPN.ST.timermodel = FM_TIMER_INTERVAL;
/*TODO*///		/* Extend handler */
/*TODO*///		FM2612[i].OPN.ST.Timer_Handler = TimerHandler;
/*TODO*///		FM2612[i].OPN.ST.IRQ_Handler   = IRQHandler;
/*TODO*///		YM2612ResetChip(i);
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- shut down emurator ----------- */
/*TODO*///void YM2612Shutdown()
/*TODO*///{
/*TODO*///    if (!FM2612) return;
/*TODO*///
/*TODO*///	FMCloseTable();
/*TODO*///	free(FM2612);
/*TODO*///	FM2612 = NULL;
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- reset one of chip ---------- */
/*TODO*///void YM2612ResetChip(int num)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	YM2612 *F2612 = &(FM2612[num]);
/*TODO*///	FM_OPN *OPN   = &(FM2612[num].OPN);
/*TODO*///
/*TODO*///	OPNSetPris( OPN , 12*12, 12*12, 0);
/*TODO*///	/* status clear */
/*TODO*///	FM_IRQMASK_SET(&OPN->ST,0x03);
/*TODO*///	OPNWriteMode(OPN,0x27,0x30); /* mode 0 , timer reset */
/*TODO*///
/*TODO*///	reset_channel( &OPN->ST , &F2612->CH[0] , 6 );
/*TODO*///
/*TODO*///	for(i = 0xb6 ; i >= 0xb4 ; i-- )
/*TODO*///	{
/*TODO*///		OPNWriteReg(OPN,i      ,0xc0);
/*TODO*///		OPNWriteReg(OPN,i|0x100,0xc0);
/*TODO*///	}
/*TODO*///	for(i = 0xb2 ; i >= 0x30 ; i-- )
/*TODO*///	{
/*TODO*///		OPNWriteReg(OPN,i      ,0);
/*TODO*///		OPNWriteReg(OPN,i|0x100,0);
/*TODO*///	}
/*TODO*///	for(i = 0x26 ; i >= 0x20 ; i-- ) OPNWriteReg(OPN,i,0);
/*TODO*///	/* DAC mode clear */
/*TODO*///	F2612->dacen = 0;
/*TODO*///}
/*TODO*///
/*TODO*////* YM2612 write */
/*TODO*////* n = number  */
/*TODO*////* a = address */
/*TODO*////* v = value   */
/*TODO*///int YM2612Write(int n, int a,UINT8 v)
/*TODO*///{
/*TODO*///	YM2612 *F2612 = &(FM2612[n]);
/*TODO*///	int addr;
/*TODO*///
/*TODO*///	switch( a&3){
/*TODO*///	case 0:	/* address port 0 */
/*TODO*///		F2612->OPN.ST.address = v & 0xff;
/*TODO*///		break;
/*TODO*///	case 1:	/* data port 0    */
/*TODO*///		addr = F2612->OPN.ST.address;
/*TODO*///		switch( addr & 0xf0 )
/*TODO*///		{
/*TODO*///		case 0x20:	/* 0x20-0x2f Mode */
/*TODO*///			switch( addr )
/*TODO*///			{
/*TODO*///			case 0x2a:	/* DAC data (YM2612) */
/*TODO*///				YM2612UpdateReq(n);
/*TODO*///				F2612->dacout = ((int)v - 0x80)<<(TL_BITS-7);
/*TODO*///				break;
/*TODO*///			case 0x2b:	/* DAC Sel  (YM2612) */
/*TODO*///				/* b7 = dac enable */
/*TODO*///				F2612->dacen = v & 0x80;
/*TODO*///				cur_chip = NULL;
/*TODO*///				break;
/*TODO*///			default:	/* OPN section */
/*TODO*///				YM2612UpdateReq(n);
/*TODO*///				/* write register */
/*TODO*///				 OPNWriteMode(&(F2612->OPN),addr,v);
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		default:	/* 0x30-0xff OPN section */
/*TODO*///			YM2612UpdateReq(n);
/*TODO*///			/* write register */
/*TODO*///			 OPNWriteReg(&(F2612->OPN),addr,v);
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	case 2:	/* address port 1 */
/*TODO*///		F2612->address1 = v & 0xff;
/*TODO*///		break;
/*TODO*///	case 3:	/* data port 1    */
/*TODO*///		addr = F2612->address1;
/*TODO*///		YM2612UpdateReq(n);
/*TODO*///		OPNWriteReg(&(F2612->OPN),addr|0x100,v);
/*TODO*///		break;
/*TODO*///	}
/*TODO*///	return F2612->OPN.ST.irq;
/*TODO*///}
/*TODO*///UINT8 YM2612Read(int n,int a)
/*TODO*///{
/*TODO*///	YM2612 *F2612 = &(FM2612[n]);
/*TODO*///
/*TODO*///	switch( a&3){
/*TODO*///	case 0:	/* status 0 */
/*TODO*///		return F2612->OPN.ST.status;
/*TODO*///	case 1:
/*TODO*///	case 2:
/*TODO*///	case 3:
/*TODO*///		Log(LOG_WAR,"YM2612 #%d:A=%d read unmapped area\n");
/*TODO*///		return F2612->OPN.ST.status;
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///int YM2612TimerOver(int n,int c)
/*TODO*///{
/*TODO*///	YM2612 *F2612 = &(FM2612[n]);
/*TODO*///
/*TODO*///	if( c )
/*TODO*///	{	/* Timer B */
/*TODO*///		TimerBOver( &(F2612->OPN.ST) );
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* Timer A */
/*TODO*///		YM2612UpdateReq(n);
/*TODO*///		/* timer update */
/*TODO*///		TimerAOver( &(F2612->OPN.ST) );
/*TODO*///		/* CSM mode key,TL controll */
/*TODO*///		if( F2612->OPN.ST.mode & 0x80 )
/*TODO*///		{	/* CSM mode total level latch and auto key on */
/*TODO*///			CSMKeyControll( &(F2612->CH[2]) );
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return F2612->OPN.ST.irq;
/*TODO*///}
/*TODO*///
/*TODO*///#endif /* BUILD_YM2612 */
/*TODO*///
/*TODO*///
/*TODO*///#if BUILD_YM2151
/*TODO*////*******************************************************************************/
/*TODO*////*		YM2151 local section                                                   */
/*TODO*////*******************************************************************************/
/*TODO*////* -------------------------- OPM ---------------------------------- */
/*TODO*///#undef  FM_SEG_SUPPORT
/*TODO*///#define FM_SEG_SUPPORT 0	/* OPM has not SEG type envelope */
/*TODO*///

    static YM2151[] FMOPM = null;/* array of YM2151's */

    static int YM2151NumChips;/* total chip */

    /* current chip state */
    static long NoiseCnt, NoiseIncr;

    static IntSubArray[] NOISE_TABLE = new IntSubArray[SIN_ENT];

    static int DT2_TABLE[] = { /* 4 DT2 values */
        /*
         *   DT2 defines offset in cents from base note
         *
         *   The table below defines offset in deltas table...
         *   User's Manual page 22
         *   Values below were calculated using formula:  value = orig.val * 1.5625
         *
         * DT2=0 DT2=1 DT2=2 DT2=3
         * 0     600   781   950
         */
        0, 384, 500, 608
    };

    static int KC_TO_SEMITONE[] = {
        /*translate note code KC into more usable number of semitone*/
        0 * 64, 1 * 64, 2 * 64, 3 * 64,
        3 * 64, 4 * 64, 5 * 64, 6 * 64,
        6 * 64, 7 * 64, 8 * 64, 9 * 64,
        9 * 64, 10 * 64, 11 * 64, 12 * 64
    };

    /* ---------- frequency counter  ---------- */
    public static void OPM_CALC_FCOUNT(YM2151 OPM, FM_CH CH) {
        if (CH.SLOT[SLOT1].Incr == -1) {
            int fc = (int) CH.fc;
            int kc = CH.kcode;

            CALC_FCSLOT(CH.SLOT[SLOT1], (int) (OPM.KC_TABLE[fc + CH.SLOT[SLOT1].DT2]), kc);
            CALC_FCSLOT(CH.SLOT[SLOT2], (int) (OPM.KC_TABLE[fc + CH.SLOT[SLOT2].DT2]), kc);
            CALC_FCSLOT(CH.SLOT[SLOT3], (int) (OPM.KC_TABLE[fc + CH.SLOT[SLOT3].DT2]), kc);
            CALC_FCSLOT(CH.SLOT[SLOT4], (int) (OPM.KC_TABLE[fc + CH.SLOT[SLOT4].DT2]), kc);
        }
    }
    /* ---------- calcrate one of channel7 ---------- */

    public static void OPM_CALC_CH7(FM_CH CH) {
        long eg_out1, eg_out2, eg_out3, eg_out4;  //envelope output
/*TODO*///
/*TODO*///	/* Phase Generator */
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	INT32 pms = lfo_pmd * CH->pms / LFO_RATE;
/*TODO*///	if(pms)
/*TODO*///	{
/*TODO*///		pg_in1 = (CH->SLOT[SLOT1].Cnt += CH->SLOT[SLOT1].Incr + (INT32)(pms * CH->SLOT[SLOT1].Incr) / PMS_RATE);
/*TODO*///		pg_in2 = (CH->SLOT[SLOT2].Cnt += CH->SLOT[SLOT2].Incr + (INT32)(pms * CH->SLOT[SLOT2].Incr) / PMS_RATE);
/*TODO*///		pg_in3 = (CH->SLOT[SLOT3].Cnt += CH->SLOT[SLOT3].Incr + (INT32)(pms * CH->SLOT[SLOT3].Incr) / PMS_RATE);
/*TODO*///		pg_in4 = (CH->SLOT[SLOT4].Cnt += CH->SLOT[SLOT4].Incr + (INT32)(pms * CH->SLOT[SLOT4].Incr) / PMS_RATE);
/*TODO*///	}
/*TODO*///	else
/*TODO*///#endif
/*TODO*///	{
        pg_in1[0] = (int) (CH.SLOT[SLOT1].Cnt += CH.SLOT[SLOT1].Incr);
        pg_in2[0] = (int) (CH.SLOT[SLOT2].Cnt += CH.SLOT[SLOT2].Incr);
        pg_in3[0] = (int) (CH.SLOT[SLOT3].Cnt += CH.SLOT[SLOT3].Incr);
        pg_in4[0] = (int) (CH.SLOT[SLOT4].Cnt += CH.SLOT[SLOT4].Incr);
        /*TODO*///	}

        /* Envelope Generator */
        eg_out1 = FM_CALC_EG(CH.SLOT[SLOT1]);
        eg_out2 = FM_CALC_EG(CH.SLOT[SLOT2]);
        eg_out3 = FM_CALC_EG(CH.SLOT[SLOT3]);
        eg_out4 = FM_CALC_EG(CH.SLOT[SLOT4]);
        /* connection */
        if (eg_out1 < EG_CUT_OFF) /* SLOT 1 */ {
            if (CH.FB != 0) {
                /* with self feed back */
                pg_in1[0] += (CH.op1_out[0] + CH.op1_out[1]) >> CH.FB;
                CH.op1_out[1] = CH.op1_out[0];
            }
            CH.op1_out[0] = OP_OUT(pg_in1[0], (int) eg_out1);
            /* output slot1 */
            if (CH.connect1 == null) {
                /* algorythm 5  */
                pg_in2[0] += CH.op1_out[0];
                pg_in3[0] += CH.op1_out[0];
                pg_in4[0] += CH.op1_out[0];
            } else {
                /* other algorythm */
                CH.connect1.write(0, CH.connect1.read(0) + CH.op1_out[0]);//*CH->connect1 += CH->op1_out[0];
            }
        }
        if (eg_out2 < EG_CUT_OFF) {	/* SLOT 2 */

            CH.connect2.write(0, CH.connect2.read(0) + OP_OUT(pg_in2[0], (int) eg_out2));//*CH->connect2 += OP_OUT(pg_in2,eg_out2);
        }
        if (eg_out3 < EG_CUT_OFF) {	/* SLOT 3 */

            CH.connect3.write(0, CH.connect3.read(0) + OP_OUT(pg_in3[0], (int) eg_out3));//*CH->connect3 += OP_OUT(pg_in3,eg_out3);
        }
        /* SLOT 4 */
        if (NoiseIncr != 0) {
            NoiseCnt += NoiseIncr;
            if (eg_out4 < EG_CUT_OFF) {
                CH.connect4.write(0, CH.connect4.read(0) + OP_OUTN((int) NoiseCnt, (int) eg_out4));//*CH->connect4 += OP_OUTN(NoiseCnt,eg_out4);
            }
        } else {
            if (eg_out4 < EG_CUT_OFF) {
                CH.connect4.write(0, CH.connect4.read(0) + OP_OUT(pg_in4[0], (int) eg_out4));//*CH->connect4 += OP_OUT(pg_in4,eg_out4);
            }
        }
    }

    /* ---------- priscaler set(and make time tables) ---------- */
    static void OPMInitTable(int num) {
        YM2151 OPM = (FMOPM[num]);
        int i;
        double pom;
        double rate;

        if (FMOPM[num].ST.rate != 0) {
            rate = (double) (1 << FREQ_BITS) / (3579545.0 / FMOPM[num].ST.clock * FMOPM[num].ST.rate);
        } else {
            rate = 1;
        }

        for (i = 0; i < 8 * 12 * 64 + 950; i++) {
            /* This calculation type was used from the Jarek's YM2151 emulator */
            pom = 6.875 * Math.pow(2, ((i + 4 * 64) * 1.5625 / 1200.0)); /*13.75Hz is note A 12semitones below A-0, so D#0 is 4 semitones above then*/
            /*calculate phase increment for above precounted Hertz value*/

            OPM.KC_TABLE[i] = (long) (pom * rate);
            /*Log(LOG_WAR,"OPM KC %d = %x\n",i,OPM->KC_TABLE[i]);*/
        }

        /* make time tables */
        init_timetables(OPM.ST, OPN_DTTABLE, OPM_ARRATE, OPM_DRRATE);
        /*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	/* LFO wave table */
/*TODO*///	for(i=0;i<LFO_ENT;i++)
/*TODO*///	{
/*TODO*///		OPM->LFO_wave[          i]= LFO_RATE * i / LFO_ENT /127;
/*TODO*///		OPM->LFO_wave[LFO_ENT  +i]= ( i<LFO_ENT/2 ? 0 : LFO_RATE )/127;
/*TODO*///		OPM->LFO_wave[LFO_ENT*2+i]= LFO_RATE* (i<LFO_ENT/2 ? i : LFO_ENT-i) /(LFO_ENT/2) /127;
/*TODO*///		OPM->LFO_wave[LFO_ENT*3+i]= LFO_RATE * (rand()&0xff) /256 /127;
/*TODO*///	}
/*TODO*///#endif
	/* NOISE wave table */
        for (i = 0; i < SIN_ENT; i++) {
            int sign = (rand() & 1) != 0 ? TL_MAX : 0;
            int lev = rand() & 0x1ff;
            //pom = lev ? 20*log10(0x200/lev) : 0;   /* decibel */
            //NOISE_TABLE[i] = &TL_TABLE[sign + (int)(pom / EG_STEP)]; /* TL_TABLE steps */
            NOISE_TABLE[i] = new IntSubArray(TL_TABLE, sign + lev * EG_ENT / 0x200); //&TL_TABLE[sign + lev * EG_ENT/0x200]; /* TL_TABLE steps */
        }
    }

    /* ---------- write a register on YM2151 chip number 'n' ---------- */
    static void OPMWriteReg(int n, int r, int v) {
        int /*UINT8*/ c;
        FM_CH CH;
        FM_SLOT SLOT;

        YM2151 OPM = (FMOPM[n]);

        c = OPM_CHAN(r);
        CH = OPM.CH[c];
        SLOT = CH.SLOT[OPM_SLOT(r)];

        switch (r & 0xe0) {
            case 0x00: /* 0x00-0x1f */

                switch (r) {
                    /*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		case 0x01:	/* test */
/*TODO*///			if( (OPM->testreg&(OPM->testreg^v))&0x02 ) /* fall eggge */
/*TODO*///			{	/* reset LFO counter */
/*TODO*///				OPM->LFOCnt = 0;
/*TODO*///				cur_chip = NULL;
/*TODO*///			}
/*TODO*///			OPM->testreg = v;
/*TODO*///			break;
/*TODO*///#endif
                    case 0x08:	/* key on / off */

                        c = v & 7;
                        /* CSM mode */
                        if ((OPM.ST.mode & 0x80) != 0) {
                            break;
                        }
                        CH = OPM.CH[c];
                        if ((v & 0x08) != 0) {
                            FM_KEYON(CH, SLOT1);
                        } else {
                            FM_KEYOFF(CH, SLOT1);
                        }
                        if ((v & 0x10) != 0) {
                            FM_KEYON(CH, SLOT2);
                        } else {
                            FM_KEYOFF(CH, SLOT2);
                        }
                        if ((v & 0x20) != 0) {
                            FM_KEYON(CH, SLOT3);
                        } else {
                            FM_KEYOFF(CH, SLOT3);
                        }
                        if ((v & 0x40) != 0) {
                            FM_KEYON(CH, SLOT4);
                        } else {
                            FM_KEYOFF(CH, SLOT4);
                        }
                        break;
                    case 0x0f:	/* Noise freq (ch7.op4) */
                        /* b7 = Noise enable */
                        /* b0-4 noise freq  */

                        OPM.NoiseIncr = (v & 0x80) == 0 ? (long) 0
                                : /* !!!!! unknown noise freqency rate !!!!! */ (long) ((1 << FREQ_BITS) / 65536 * (v & 0x1f) * OPM.ST.freqbase);
                        cur_chip = null;
                        break;
                    case 0x10:	/* timer A High 8*/

                        OPM.ST.TA = (OPM.ST.TA & 0x03) | (((int) v) << 2);
                        break;
                    case 0x11:	/* timer A Low 2*/

                        OPM.ST.TA = (OPM.ST.TA & 0x3fc) | (v & 3);
                        break;
                    case 0x12:	/* timer B */

                        OPM.ST.TB = v;
                        break;
                    case 0x14:	/* mode , timer controll */

                        FMSetMode((OPM.ST), n, v);
                        break;
                    /*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		case 0x18:	/* lfreq   */
/*TODO*///			/* f = fm * 2^(LFRQ/16) / (4295*10^6) */
/*TODO*///			{
/*TODO*///				static double drate[16]={
/*TODO*///					1.0        ,1.044273782,1.090507733,1.138788635, //0-3
/*TODO*///					1.189207115,1.241857812,1.296839555,1.354255547, //4-7
/*TODO*///					1.414213562,1.476826146,1.542210825,1.610490332, //8-11
/*TODO*///					1.681792831,1.75625216 ,1.834008086,1.915206561};
/*TODO*///				double rate = pow(2.0,v/16)*drate[v&0x0f] / 4295000000.0;
/*TODO*///				OPM->LFOIncr = (double)LFO_ENT*(1<<LFO_SHIFT) * (OPM->ST.freqbase*64) * rate;
/*TODO*///				cur_chip = NULL;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 0x19:	/* PMD/AMD */
/*TODO*///			if( v & 0x80 ) OPM->pmd = v & 0x7f;
/*TODO*///			else           OPM->amd = v & 0x7f;
/*TODO*///			break;
/*TODO*///#endif
                    case 0x1b: /* CT , W  */ /* b7 = CT1 */ /* b6 = CT0 */ /* b0-2 = wave form(LFO) 0=nokogiri,1=houkei,2=sankaku,3=noise */ //if(OPM->ct != v)
                    {
                        OPM.ct = v >> 6;
                        if (OPM.PortWrite != null) {
                            OPM.PortWrite.handler(0, OPM.ct); /* bit0 = CT0,bit1 = CT1 */

                        }
                    }
                    /*TODO*///#if FM_LFO_SUPPORT
/*TODO*///			if( OPM->wavetype != &OPM->LFO_wave[(v&3)*LFO_ENT])
/*TODO*///			{
/*TODO*///				OPM->wavetype = &OPM->LFO_wave[(v&3)*LFO_ENT];
/*TODO*///				cur_chip = NULL;
/*TODO*///			}
/*TODO*///#endif
                    break;
                }
                break;
            case 0x20:	/* 20-3f */

                switch (OPM_SLOT(r)) {
                    case 0: /* 0x20-0x27 : RL,FB,CON */ {
                        int feedback = (v >> 3) & 7;
                        CH.ALGO = v & 7;
                        CH.FB = feedback != 0 ? 8 + 1 - feedback : 0;
                        /* RL order -> LR order */
                        CH.PAN = ((v >> 7) & 1) | ((v >> 5) & 2);
                        setup_connection(CH);
                    }
                    break;
                    case 1: /* 0x28-0x2f : Keycode */ {
                        int blk = (v >> 4) & 7;
                        /* make keyscale code */
                        CH.kcode = (v >> 2) & 0x1f;
                        /* make basic increment counter 22bit = 1 cycle */
                        CH.fc = (blk * (12 * 64)) + KC_TO_SEMITONE[v & 0x0f] + CH.fn_h;
                        CH.SLOT[SLOT1].Incr = -1;
                    }
                    break;
                    case 2: /* 0x30-0x37 : Keyfunction */

                        CH.fc -= CH.fn_h;
                        CH.fn_h = v >> 2;
                        CH.fc += CH.fn_h;
                        CH.SLOT[SLOT1].Incr = -1;
                        break;
                    /*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		case 3: /* 0x38-0x3f : PMS / AMS */
/*TODO*///			/* b0-1 AMS */
/*TODO*///			/* AMS * 23.90625db @ AMD=127 */
/*TODO*///			//CH->ams = (v & 0x03) * (23.90625/EG_STEP);
/*TODO*///			CH->ams = (23.90625/EG_STEP) / (1<<(3-(v&3)));
/*TODO*///			CH->SLOT[SLOT1].ams = CH->ams * CH->SLOT[SLOT1].amon;
/*TODO*///			CH->SLOT[SLOT2].ams = CH->ams * CH->SLOT[SLOT2].amon;
/*TODO*///			CH->SLOT[SLOT3].ams = CH->ams * CH->SLOT[SLOT3].amon;
/*TODO*///			CH->SLOT[SLOT4].ams = CH->ams * CH->SLOT[SLOT4].amon;
/*TODO*///			/* b4-6 PMS */
/*TODO*///			/* 0,5,10,20,50,100,400,700 (cent) @ PMD=127 */
/*TODO*///			{
/*TODO*///				/* 1 octabe = 1200cent = +100%/-50% */
/*TODO*///				/* 100cent  = 1seminote = 6% ?? */
/*TODO*///				static const int pmd_table[8] = {0,5,10,20,50,100,400,700};
/*TODO*///				CH->pms = (1.5/1200.0)*pmd_table[(v>>4) & 0x07] * PMS_RATE;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///#endif
                }
                break;
            case 0x40:	/* DT1,MUL */

                set_det_mul(OPM.ST, CH, SLOT, v);
                break;
            case 0x60:	/* TL */

                set_tl(CH, SLOT, v, (((c == 7) && (OPM.ST.mode & 0x80) != 0) ? 1 : 0));
                break;
            case 0x80:	/* KS, AR */

                set_ar_ksr(CH, SLOT, v, OPM.ST.AR_TABLE);
                break;
            case 0xa0:	/* AMS EN,D1R */

                set_dr(SLOT, v, OPM.ST.DR_TABLE);
                /*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		/* bit7 = AMS ENABLE */
/*TODO*///		SLOT->amon = v>>7;
/*TODO*///		SLOT->ams = CH->ams * SLOT->amon;
/*TODO*///#endif
                break;
            case 0xc0:	/* DT2 ,D2R */

                SLOT.DT2 = DT2_TABLE[v >> 6];
                CH.SLOT[SLOT1].Incr = -1;
                set_sr(SLOT, v, OPM.ST.DR_TABLE);
                break;
            case 0xe0:	/* D1L, RR */

                set_sl_rr(SLOT, v, OPM.ST.DR_TABLE);
                break;
        }
    }
    /*TODO*///
/*TODO*////* ---------- read status port ---------- */
/*TODO*///static UINT8 OPMReadStatus(int n)
/*TODO*///{
/*TODO*///	return FMOPM[n].ST.status;
/*TODO*///}
/*TODO*///

    public static int YM2151Write(int n, int a,/*UINT8*/ int v) {
        YM2151 F2151 = (FMOPM[n]);

        if ((a & 1) == 0) {	/* address port */

            F2151.ST.address = (v & 0xff);
        } else {	/* data port */

            int addr = F2151.ST.address;
            YM2151UpdateRequest(n);
            /* write register */
            OPMWriteReg(n, addr, v);
        }
        return F2151.ST.irq;

    }
    /* ---------- reset one of chip ---------- */

    public static void OPMResetChip(int num) {
        int i;
        YM2151 OPM = FMOPM[num];

        OPMInitTable(num);
        reset_channel(OPM.ST, /*&OPM->CH[0]*/ OPM.CH, 8);
        /* status clear */
        FM_IRQMASK_SET(OPM.ST, 0x03);
        OPMWriteReg(num, 0x1b, 0x00);
        /* reset OPerator paramater */
        for (i = 0xff; i >= 0x20; i--) {
            OPMWriteReg(num, i, 0);
        }
    }
    /* ----------  Initialize YM2151 emulator(s) ----------    */
    /* 'num' is the number of virtual YM2151's to allocate     */
    /* 'rate' is sampling rate and 'bufsiz' is the size of the */
    /* buffer that should be updated at each interval          */

    public static int OPMInit(int num, int clock, int rate, FM_TIMERHANDLERtr TimerHandler, FM_IRQHANDLEPtr IRQHandler) {
        if (FMOPM != null) {
            return (-1);	/* duplicate init. */

        }
        cur_chip = null;	/* hiro-shi!! */

        YM2151NumChips = num;

        /* allocate ym2151 state space */
        FMOPM = new YM2151[YM2151NumChips];

        for (int i = 0; i < YM2151NumChips; i++) {
            FMOPM[i] = new YM2151();
        }

        /* allocate total lebel table (128kb space) */
        if (FMInitTable() == 0) {
            FMOPM = null;
            return (-1);
        }
        for (int i = 0; i < YM2151NumChips; i++) {
            FMOPM[i].ST.index = i;
            FMOPM[i].ST.clock = clock;
            FMOPM[i].ST.rate = rate;
            /* FMOPM[i].ST.irq  = 0; */
            /* FMOPM[i].ST.status = 0; */
            FMOPM[i].ST.timermodel = FM_TIMER_INTERVAL;
            FMOPM[i].ST.freqbase = rate != 0 ? ((double) clock / rate) / 64 : 0;
            FMOPM[i].ST.TimerBase = 1.0 / ((double) clock / 64.0);
            /* Extend handler */
            FMOPM[i].ST.Timer_Handler = TimerHandler;
            FMOPM[i].ST.IRQ_Handler = IRQHandler;
            /* Reset callback handler of CT0/1 */
            FMOPM[i].PortWrite = null;
            OPMResetChip(i);
        }
        return (0);
    }
    /* ---------- shut down emurator ----------- */

    public static void OPMShutdown() {
        if (FMOPM == null) {
            return;
        }

        FMCloseTable();
        FMOPM = null;
    }

    public static int/*UINT8*/ YM2151Read(int n, int a) {
        if ((a & 1) == 0) {
            return 0;
        } else {
            return FMOPM[n].ST.status;
        }
    }
    /* ---------- make digital sound data ---------- */
    public static streams.StreamInitMultiPtr OPMUpdateOne = new streams.StreamInitMultiPtr() {
        public void handler(int num, ShortPtr[] buffer, int length) {
            YM2151 OPM = (FMOPM[num]);
            int i;
            int amd, pmd;
            FM_CH ch;
            ShortPtr bufL, bufR;

            /* set bufer */
            bufL = buffer[0];
            bufR = buffer[1];

            if ((Object) OPM != cur_chip) {
                cur_chip = OPM;

                State = OPM.ST;
                /* channel pointer */
                cch[0] = OPM.CH[0];
                cch[1] = OPM.CH[1];
                cch[2] = OPM.CH[2];
                cch[3] = OPM.CH[3];
                cch[4] = OPM.CH[4];
                cch[5] = OPM.CH[5];
                cch[6] = OPM.CH[6];
                cch[7] = OPM.CH[7];
                /* ch7.op4 noise mode / step */
                NoiseIncr = OPM.NoiseIncr;
                NoiseCnt = OPM.NoiseCnt;
                /*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		/* LFO */
/*TODO*///		LFOCnt  = OPM->LFOCnt;
/*TODO*///		//LFOIncr = OPM->LFOIncr;
/*TODO*///		if( !LFOIncr ) lfo_amd = lfo_pmd = 0;
/*TODO*///		LFO_wave = OPM->wavetype;
/*TODO*///#endif
            }
            /*TODO*///	amd = OPM->amd;
/*TODO*///	pmd = OPM->pmd;
/*TODO*///	if(amd==0 && pmd==0)
/*TODO*///		LFOIncr = 0;
/*TODO*///	else
/*TODO*///		LFOIncr = OPM->LFOIncr;
/*TODO*///
            OPM_CALC_FCOUNT(OPM, cch[0]);
            OPM_CALC_FCOUNT(OPM, cch[1]);
            OPM_CALC_FCOUNT(OPM, cch[2]);
            OPM_CALC_FCOUNT(OPM, cch[3]);
            OPM_CALC_FCOUNT(OPM, cch[4]);
            OPM_CALC_FCOUNT(OPM, cch[5]);
            OPM_CALC_FCOUNT(OPM, cch[6]);
            /* CSM check */
            OPM_CALC_FCOUNT(OPM, cch[7]);

            for (i = 0; i < length; i++) {
                /*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		/* LFO */
/*TODO*///		if( LFOIncr )
/*TODO*///		{
/*TODO*///			INT32 depth = LFO_wave[(LFOCnt+=LFOIncr)>>LFO_SHIFT];
/*TODO*///			lfo_amd = depth * amd;
/*TODO*///			lfo_pmd = (depth-(LFO_RATE/127/2)) * pmd;
/*TODO*///		}
/*TODO*///#endif
		/* clear output acc. */
                out_ch[OUTD_LEFT] = out_ch[OUTD_RIGHT] = out_ch[OUTD_CENTER] = 0;
                /* calcrate channel output */
                for (int k = 0; k < 6; k++)//for(ch = cch[0] ; ch <= cch[6] ; ch++)
                {
                    ch = cch[k];
                    FM_CALC_CH(ch);//FM_( ch );
                }

                OPM_CALC_CH7(cch[7]);
                /* buffering */
                //FM_BUFFERING_STEREO;
                /* stereo separate */
                {
                    /* get left & right output with clipping */
                    out_ch[OUTD_LEFT] += out_ch[OUTD_CENTER];
                    //Limit(ref  out_ch[OUTD_LEFT], FM_MAXOUT, FM_MINOUT);
                    out_ch[OUTD_RIGHT] += out_ch[OUTD_CENTER];
                    //Limit(ref  out_ch[OUTD_RIGHT], FM_MAXOUT, FM_MINOUT);
                        /* buffering */
                    bufL.write(i, (short) (out_ch[OUTD_LEFT] >> FM_OUTSB));
                    bufR.write(i, (short) (out_ch[OUTD_RIGHT] >> FM_OUTSB));
                }
                /* timer A controll */
                INTERNAL_TIMER_A(State, cch[7]);
            }
            INTERNAL_TIMER_B(State, length);
            OPM.NoiseCnt = NoiseCnt;
            /*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	OPM->LFOCnt = LFOCnt;
/*TODO*///#endif
        }
    };

    public static void OPMSetPortHander(int n, WriteHandlerPtr PortWrite/*void (*PortWrite)(int offset,int CT)*/) {
        FMOPM[n].PortWrite = PortWrite;
    }

    public static int YM2151TimerOver(int n, int c) {
        throw new UnsupportedOperationException("Unsupported");
        /*TODO*///	YM2151 *F2151 = &(FMOPM[n]);
/*TODO*///
/*TODO*///	if( c )
/*TODO*///	{	/* Timer B */
/*TODO*///		TimerBOver( &(F2151->ST) );
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* Timer A */
/*TODO*///		YM2151UpdateReq(n);
/*TODO*///		/* timer update */
/*TODO*///		TimerAOver( &(F2151->ST) );
/*TODO*///		/* CSM mode key,TL controll */
/*TODO*///		if( F2151->ST.mode & 0x80 )
/*TODO*///		{	/* CSM mode total level latch and auto key on */
/*TODO*///			CSMKeyControll( &(F2151->CH[0]) );
/*TODO*///			CSMKeyControll( &(F2151->CH[1]) );
/*TODO*///			CSMKeyControll( &(F2151->CH[2]) );
/*TODO*///			CSMKeyControll( &(F2151->CH[3]) );
/*TODO*///			CSMKeyControll( &(F2151->CH[4]) );
/*TODO*///			CSMKeyControll( &(F2151->CH[5]) );
/*TODO*///			CSMKeyControll( &(F2151->CH[6]) );
/*TODO*///			CSMKeyControll( &(F2151->CH[7]) );
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return F2151->ST.irq;
    }

}
