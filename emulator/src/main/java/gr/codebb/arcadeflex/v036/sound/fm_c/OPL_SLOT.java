package gr.codebb.arcadeflex.v036.sound.fm_c;

import static gr.codebb.arcadeflex.v036.platform.libc.*;

public class OPL_SLOT {
    public int TL;		/* total level     :TL << 8            */
    public int TLL;		/* adjusted now TL                     */
    public int /*UINT8*/  KSR;		/* key scale rate  :(shift down bit)   */
    public IntSubArray AR;		/* attack rate     :&AR_TABLE[AR<<2]   */
    public IntSubArray DR;		/* decay rate      :&DR_TALBE[DR<<2]   */
    public int SL;		/* sustin level    :SL_TALBE[SL]       */
    public IntSubArray RR;		/* release rate    :&DR_TABLE[RR<<2]   */
    public int /*UINT8*/ ksl;		/* keyscale level  :(shift down bits)  */
    public int /*UINT8*/ ksr;		/* key scale rate  :kcode>>KSR         */
    public long /*UINT32*/ mul;		/* multiple        :ML_TABLE[ML]       */
    public long Cnt;		/* frequency count :                   */
    public long /*UINT32*/ Incr;	/* frequency step  :                   */
    	/* envelope generator state */
    public int /*UINT8*/ eg_typ;	/* envelope type flag                  */
    public int /*UINT8*/ evm;		/* envelope phase                      */
    public int evc;		/* envelope counter                    */
    public int eve;		/* envelope counter end point          */
    public int evs;		/* envelope counter step               */
    public int evsa;	/* envelope step for AR :AR[ksr]       */
    public int evsd;	/* envelope step for DR :DR[ksr]       */
    public int evsr;	/* envelope step for RR :RR[ksr]       */
    	/* LFO */
    public int /*UINT8*/ ams;		/* ams flag                            */
    public int /*UINT8*/ vib;		/* vibrate flag                        */
    /* wave selector */
    public IntSubArray[] wavetable;
    public int wt_offset = 0;   
}
