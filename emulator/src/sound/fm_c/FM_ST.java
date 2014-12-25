package sound.fm_c;

import static sound.fmH.*;

public class FM_ST {

    public int /*UINT8*/ index;		/* chip index (number of chip) */

    public int clock;			/* master clock  (Hz)  */

    public int rate;			/* sampling rate (Hz)  */
    /*TODO*///	double freqbase;	/* frequency base      */
/*TODO*///	double TimerBase;	/* Timer base time     */
/*TODO*///	UINT8 address;		/* address register    */
/*TODO*///	UINT8 irq;			/* interrupt level     */
/*TODO*///	UINT8 irqmask;		/* irq mask            */
/*TODO*///	UINT8 status;		/* status flag         */
/*TODO*///	UINT32 mode;		/* mode  CSM / 3SLOT   */
/*TODO*///	int TA;				/* timer a             */
/*TODO*///	int TAC;			/* timer a counter     */
/*TODO*///	UINT8 TB;			/* timer b             */
/*TODO*///	int TBC;			/* timer b counter     */
/*TODO*///	/* speedup customize */
/*TODO*///	/* local time tables */
/*TODO*///	INT32 DT_TABLE[8][32];	/* DeTune tables       */
/*TODO*///	INT32 AR_TABLE[94];		/* Atttack rate tables */
/*TODO*///	INT32 DR_TABLE[94];		/* Decay rate tables   */
/* Extention Timer and IRQ handler */

    public FM_TIMERHANDLERtr Timer_Handler;
    public FM_IRQHANDLEPtr IRQ_Handler;
    /* timer model single / interval */
    public int /*UINT8*/ timermodel;
}
