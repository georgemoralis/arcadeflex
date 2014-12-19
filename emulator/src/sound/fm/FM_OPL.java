/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sound.fm;

import static sound.fmoplH.*;


/**
 *
 * @author shadow
 */
public class FM_OPL {

    public FM_OPL() {
        AR_TABLE = new int[75];
        DR_TABLE = new int[75];
        FN_TABLE = new long[1024];
    }
    public int/*UINT8*/ type;			/* chip type                        */
    public int clock;			/* master clock  (Hz)                */
    public int rate;			/* sampling rate (Hz)                */
    public double freqbase;	/* frequency base                    */
    public double TimerBase;	/* Timer base time (==sampling time) */
    public int /*UINT8*/ address;		/* address register                  */
    public int /*UINT8*/ status;		/* status flag                       */
    /*TODO*///	UINT8 statusmask;	/* status mask                       */
    /*TODO*///	UINT32 mode;		/* Reg.08 : CSM , notesel,etc.       */
    /*TODO*///	/* Timer */
    /*TODO*///	int T[2];			/* timer counter       */
    /*TODO*///	UINT8 st[2];		/* timer enable        */
    /*TODO*///	/* FM channel slots */
    /*TODO*///	OPL_CH *P_CH;		/* pointer of CH       */

    public int max_ch;			/* maximum channel     */
    /*TODO*///	/* Rythm sention */
    /*TODO*///	UINT8 rythm;		/* Rythm mode , key flag */
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
    /*TODO*///	/* LFO */
    /*TODO*///	INT32 *ams_table;
    /*TODO*///	INT32 *vib_table;
    public int amsCnt;
    public int amsIncr;
    public int vibCnt;
    public int vibIncr;
    /*TODO*///	/* wave selector enable flag */
    /*TODO*///	UINT8 wavesel;
    	/* external event callback handler */

    public OPL_TIMERHANDLERPtr TimerHandler;		/* TIMER handler   */
    public int TimerParam;						/* TIMER parameter */
    public OPL_IRQHANDLERPtr IRQHandler;		/* IRQ handler    */
    public int IRQParam;						/* IRQ parameter  */
    public OPL_UPDATEHANDLERPtr UpdateHandler;	/* stream update handler   */
    public int UpdateParam;					/* stream update parameter */


}
