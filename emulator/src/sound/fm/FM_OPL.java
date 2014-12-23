package sound.fm;

import static sound.fmoplH.*;
import static sound.fm.OPL_CH.*;
import static arcadeflex.libc.*;


public class FM_OPL {

    public FM_OPL() {
        AR_TABLE = new int[75];
        DR_TABLE = new int[75];
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
    /*TODO*///	/* Timer */
    public int[] T;			/* timer counter       */
    public int[]/*UINT8*/ st;		/* timer enable        */
    /*TODO*///	/* FM channel slots */
    	public OPL_CH[] P_CH;		/* pointer of CH       */

    public int max_ch;			/* maximum channel     */
    	/* Rythm sention */
    public int /*UINT8*/ rythm;		/* Rythm mode , key flag */
    /*TODO*///#if BUILD_Y8950
    /*TODO*///	/* Delta-T ADPCM unit (Y8950) */
    /*TODO*///	YM_DELTAT *deltat;			/* DELTA-T ADPCM       */
    /*TODO*///#endif
    /*TODO*///	/* Keyboard / I/O interface unit (Y8950) */
    /*TODO*///	UINT8 portDirection;
    /*TODO*///	UINT8 portLatch;
    /*TODO*///	OPL_PORTHANDLER_R porthandler_r;
    /*TODO*///	OPL_PORTHANDLER_W porthandler_w;
    /*TODO*///	int port_param;
    /*TODO*///	OPL_PORTHANDLER_R keyboardhandler_r;
    /*TODO*///	OPL_PORTHANDLER_W keyboardhandler_w;
    /*TODO*///	int keyboard_param;
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
