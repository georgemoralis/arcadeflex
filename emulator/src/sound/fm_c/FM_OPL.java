package sound.fm_c;

import static sound.fmoplH.*;
import static sound.fm_c.OPL_CH.*;
import static arcadeflex.libc.*;
import sound.YM_DELTAT;

public class FM_OPL {

    public FM_OPL() {
        AR_TABLE = new int[76];
        DR_TABLE = new int[76];
        FN_TABLE = new long[1024];
        T=new int[2];
        st=new int[2];
    }
    public int/*UINT8*/ type;			/* chip type                        */
    public int clock;			/* master clock  (Hz)                */
    public int rate;			/* sampling rate (Hz)                */
    public double freqbase;	/* frequency base                    */
    public double TimerBase;	/* Timer base time (==sampling time) */
    public int /*UINT8*/ address;		/* address register                  */
    public int /*UINT8*/ status;		/* status flag                       */
    public int /*UINT8*/ statusmask;	/* status mask                       */
    public int /*UINT32*/ mode;		/* Reg.08 : CSM , notesel,etc.       */
    	/* Timer */
    public int[] T;			/* timer counter       */
    public int[]/*UINT8*/ st;		/* timer enable        */
    /* FM channel slots */
    public OPL_CH[] P_CH;		/* pointer of CH       */

    public int max_ch;			/* maximum channel     */
    	/* Rythm sention */
    public int /*UINT8*/ rythm;		/* Rythm mode , key flag */
    
    /* Delta-T ADPCM unit (Y8950) */
    public YM_DELTAT deltat;			/* DELTA-T ADPCM       */

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

    public int[] AR_TABLE;	/* atttack rate tables */

    public int[] DR_TABLE;	/* decay rate tables   */
    public /*UINT32*/long[] FN_TABLE;  /* fnumber -> increment counter */
    	/* LFO */
    public IntSubArray ams_table;
    public IntSubArray vib_table;
    public int amsCnt;
    public int amsIncr;
    public int vibCnt;
    public int vibIncr;
    /* wave selector enable flag */
    public int	/*UINT8*/ wavesel;
    	/* external event callback handler */

    public OPL_TIMERHANDLERPtr TimerHandler;		/* TIMER handler   */
    public int TimerParam;						/* TIMER parameter */
    public OPL_IRQHANDLERPtr IRQHandler;		/* IRQ handler    */
    public int IRQParam;						/* IRQ parameter  */
    public OPL_UPDATEHANDLERPtr UpdateHandler;	/* stream update handler   */
    public int UpdateParam;					/* stream update parameter */


}
