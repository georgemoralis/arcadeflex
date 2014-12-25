package sound.fm_c;

import sound.fmH.*;


public class FM_SLOT {
    /*TODO*///	INT32 *DT;			/* detune          :DT_TABLE[DT]       */
/*TODO*///	int DT2;			/* multiple,Detune2:(DT2<<4)|ML for OPM*/
/*TODO*///	int TL;				/* total level     :TL << 8            */
/*TODO*///	UINT8 KSR;			/* key scale rate  :3-KSR              */
/*TODO*///	const INT32 *AR;	/* attack rate     :&AR_TABLE[AR<<1]   */
/*TODO*///	const INT32 *DR;	/* decay rate      :&DR_TABLE[DR<<1]   */
/*TODO*///	const INT32 *SR;	/* sustin rate     :&DR_TABLE[SR<<1]   */
public int   SL;			/* sustin level    :SL_TABLE[SL]       */
/*TODO*///	const INT32 *RR;	/* release rate    :&DR_TABLE[RR<<2+2] */

    public int /*UINT8*/ SEG;			/* SSG EG type     :SSGEG              */
    /*TODO*///	UINT8 ksr;			/* key scale rate  :kcode>>(3-KSR)     */
/*TODO*///	UINT32 mul;			/* multiple        :ML_TABLE[ML]       */
/*TODO*///	/* Phase Generator */
public long /*UINT32*/ Cnt;			/* frequency count :                   */
/*TODO*///	UINT32 Incr;		/* frequency step  :                   */
/*TODO*///	/* Envelope Generator */

    public EGPtr eg_next;	//void (*eg_next)(struct fm_slot *SLOT);	/* pointer of phase handler */

    public int evc;			/* envelope counter                    */

    public int eve;			/* envelope counter end point          */

    public int evs;			/* envelope counter step               */

    public int evsa;			/* envelope step for Attack            */

    public int evsd;			/* envelope step for Decay             */

    public int evss;			/* envelope step for Sustain           */

    public int evsr;			/* envelope step for Release           */

    public int TLL;			/* adjusted TotalLevel                 */
    /*TODO*///	/* LFO */
/*TODO*///	UINT8 amon;			/* AMS enable flag              */
/*TODO*///	UINT32 ams;			/* AMS depth level of this SLOT */   

}
