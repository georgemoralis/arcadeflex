/*
 *  Ported to 0.37b7
 */
package gr.codebb.arcadeflex.v037b7.sound;

import gr.codebb.arcadeflex.common.SubArrays.IntSubArray;
import gr.codebb.arcadeflex.v037b7.sound.ymdeltatH.YM_DELTAT;


public class fmoplH {

    public static abstract interface OPL_TIMERHANDLERPtr {

        public abstract void handler(int channel, double interval_Sec);
    }

    public static abstract interface OPL_IRQHANDLERPtr {

        public abstract void handler(int param, int irq);
    }

    public static abstract interface OPL_UPDATEHANDLERPtr {

        public abstract void handler(int param, int min_interval_us);
    }

    public static abstract interface OPL_PORTHANDLER_WPtr {

        public abstract void handler(int param,/*unsigned char*/ int data);
    }

    public static abstract interface OPL_PORTHANDLER_RPtr {

        public abstract int handler(int param);
    }

    /* !!!!! here is private section , do not access there member direct !!!!! */
    public static final int OPL_TYPE_WAVESEL = 0x01;/* waveform select    */
    public static final int OPL_TYPE_ADPCM = 0x02;/* DELTA-T ADPCM unit */
    public static final int OPL_TYPE_KEYBOARD = 0x04;/* keyboard interface */
    public static final int OPL_TYPE_IO = 0x08;/* I/O port */

    public static class OPL_SLOT {

        public int TL;/* total level     :TL << 8            */
        public int TLL;/* adjusted now TL                     */
        public int /*UINT8*/ KSR;/* key scale rate  :(shift down bit)   */
        public IntSubArray AR;/* attack rate     :&AR_TABLE[AR<<2]   */
        public IntSubArray DR;/* decay rate      :&DR_TALBE[DR<<2]   */
        public int SL;/* sustin level    :SL_TALBE[SL]       */
        public IntSubArray RR;/* release rate    :&DR_TABLE[RR<<2]   */
        public int /*UINT8*/ ksl;/* keyscale level  :(shift down bits)  */
        public int /*UINT8*/ ksr;/* key scale rate  :kcode>>KSR         */
        public long /*UINT32*/ mul;/* multiple        :ML_TABLE[ML]       */
        public long Cnt;/* frequency count :                   */
        public long /*UINT32*/ Incr;/* frequency step  :                   */
 /* envelope generator state */
        public int /*UINT8*/ eg_typ;/* envelope type flag                  */
        public int /*UINT8*/ evm;/* envelope phase                      */
        public int evc;/* envelope counter                    */
        public int eve;/* envelope counter end point          */
        public int evs;/* envelope counter step               */
        public int evsa;/* envelope step for AR :AR[ksr]       */
        public int evsd;/* envelope step for DR :DR[ksr]       */
        public int evsr;/* envelope step for RR :RR[ksr]       */
 /* LFO */
        public int /*UINT8*/ ams;/* ams flag                            */
        public int /*UINT8*/ vib;/* vibrate flag                        */
 /* wave selector */
        public IntSubArray[] wavetable;
        public int wt_offset = 0;
    }

    public static class OPL_CH {

        public OPL_CH() {
            SLOT = new OPL_SLOT[2];
            SLOT[0] = new OPL_SLOT();
            SLOT[1] = new OPL_SLOT();
            op1_out = new int[2];
        }

        public OPL_SLOT[] SLOT;
        public int /*UINT8*/ CON;/* connection type                     */
        public int /*UINT8*/ FB;/* feed back       :(shift down bit)   */
        public int[] connect1;/* slot1 output pointer                */
        public int[] connect2;/* slot2 output pointer                */
        public int[] op1_out;/* slot1 output for selfeedback        */
 /* phase generator state */
        public long /*UINT32*/ block_fnum;
        /* block+fnum      :                   */
        public int /*UINT8*/ kcode;/* key code        : KeyScaleCode      */
        public long /*UINT32*/ fc;/* Freq. Increment base                */
        public long /*UINT32*/ ksl_base;/* KeyScaleLevel Base step             */
        public int /*UINT8*/ keyon;/* key on/off flag*/
    }

    public static class FM_OPL {

        public FM_OPL() {
            AR_TABLE = new int[76];
            DR_TABLE = new int[76];
            FN_TABLE = new long[1024];
            T = new int[2];
            st = new int[2];
        }

        public int/*UINT8*/ type;/* chip type                        */
        public int clock;/* master clock  (Hz)                */
        public int rate;/* sampling rate (Hz)                */
        public double freqbase;/* frequency base                    */
        public double TimerBase;/* Timer base time (==sampling time) */
        public int /*UINT8*/ address;/* address register                  */
        public int /*UINT8*/ status;/* status flag                       */
        public int /*UINT8*/ statusmask;/* status mask                       */
        public int /*UINT32*/ mode;
        /* Reg.08 : CSM , notesel,etc.       */
 /* Timer */
        public int[] T;/* timer counter       */
        public int[]/*UINT8*/ st;/* timer enable        */
 /* FM channel slots */
        public OPL_CH[] P_CH;/* pointer of CH       */

        public int max_ch;/* maximum channel     */
 /* Rythm sention */
        public int /*UINT8*/ rythm;/* Rythm mode , key flag */

 /* Delta-T ADPCM unit (Y8950) */
        public YM_DELTAT deltat;
        /* DELTA-T ADPCM       */
 /* Keyboard / I/O interface unit (Y8950) */
        public int /*UINT8*/ portDirection;
        public int /*UINT8*/ portLatch;
        public OPL_PORTHANDLER_RPtr porthandler_r;
        public OPL_PORTHANDLER_WPtr porthandler_w;
        public int port_param;
        public OPL_PORTHANDLER_RPtr keyboardhandler_r;
        public OPL_PORTHANDLER_WPtr keyboardhandler_w;
        public int keyboard_param;
        /* time tables */
        public int[] AR_TABLE;/* atttack rate tables */
        public int[] DR_TABLE;/* decay rate tables   */
        public /*UINT32*/ long[] FN_TABLE;/* fnumber -> increment counter */
 /* LFO */
        public IntSubArray ams_table;
        public IntSubArray vib_table;
        public int amsCnt;
        public int amsIncr;
        public int vibCnt;
        public int vibIncr;
        /* wave selector enable flag */
        public int /*UINT8*/ wavesel;/* external event callback handler */

        public OPL_TIMERHANDLERPtr TimerHandler;/* TIMER handler   */
        public int TimerParam;/* TIMER parameter */
        public OPL_IRQHANDLERPtr IRQHandler;/* IRQ handler    */
        public int IRQParam;/* IRQ parameter  */
        public OPL_UPDATEHANDLERPtr UpdateHandler;/* stream update handler   */
        public int UpdateParam;/* stream update parameter */

    }
    /* ---------- Generic interface section ---------- */
    public static final int OPL_TYPE_YM3526 = (0);
    public static final int OPL_TYPE_YM3812 = (OPL_TYPE_WAVESEL);
    public static final int OPL_TYPE_Y8950 = (OPL_TYPE_ADPCM | OPL_TYPE_KEYBOARD | OPL_TYPE_IO);
}
