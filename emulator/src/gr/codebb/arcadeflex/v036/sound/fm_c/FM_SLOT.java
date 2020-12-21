package gr.codebb.arcadeflex.v036.sound.fm_c;

import gr.codebb.arcadeflex.v036.sound.fmH.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;

public class FM_SLOT {

    public int[] DT;			/* detune          :DT_TABLE[DT]       */

    public int DT2;			/* multiple,Detune2:(DT2<<4)|ML for OPM*/

    public int TL;				/* total level     :TL << 8            */

    public int /*UINT8*/ KSR;			/* key scale rate  :3-KSR              */

    public IntSubArray AR;	/* attack rate     :&AR_TABLE[AR<<1]   */

    public IntSubArray DR;	/* decay rate      :&DR_TABLE[DR<<1]   */

    public IntSubArray SR;	/* sustin rate     :&DR_TABLE[SR<<1]   */

    public int SL;			/* sustin level    :SL_TABLE[SL]       */

    public IntSubArray RR;	/* release rate    :&DR_TABLE[RR<<2+2] */

    public int /*UINT8*/ SEG;			/* SSG EG type     :SSGEG              */

    public int /*UINT8*/ ksr;			/* key scale rate  :kcode>>(3-KSR)     */

    public long /*UINT32*/ mul;			/* multiple        :ML_TABLE[ML]       */
    /* Phase Generator */

    public long /*UINT32*/ Cnt;			/* frequency count :                   */

    public long /*UINT32*/ Incr;		/* frequency step  :                   */
    /* Envelope Generator */

    public EGPtr eg_next;	//void (*eg_next)(struct fm_slot *SLOT);	/* pointer of phase handler */

    public int evc;			/* envelope counter                    */

    public int eve;			/* envelope counter end point          */

    public int evs;			/* envelope counter step               */

    public int evsa;			/* envelope step for Attack            */

    public int evsd;			/* envelope step for Decay             */

    public int evss;			/* envelope step for Sustain           */

    public int evsr;			/* envelope step for Release           */

    public int TLL;			/* adjusted TotalLevel                 */
    /* LFO */

    public int /*UINT8*/ amon;			/* AMS enable flag              */

    public long /*UINT32*/ ams;			/* AMS depth level of this SLOT */

}
