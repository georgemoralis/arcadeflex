package sound.fm_c;

public class FM_OPN {
    public FM_OPN()
    {
        ST=new FM_ST();
    }
    public int /*UINT8*/ type;		/* chip type         */
    public	FM_ST ST;				/* general state     */
/*TODO*///	FM_3SLOT SL3;			/* 3 slot mode state */
    public FM_CH[] P_CH;			/* pointer of CH     */
/*TODO*///	UINT32 FN_TABLE[2048]; /* fnumber -> increment counter */
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	/* LFO */
/*TODO*///	UINT32 LFOCnt;
/*TODO*///	UINT32 LFOIncr;
/*TODO*///	UINT32 LFO_FREQ[8];/* LFO FREQ table */
/*TODO*///	INT32 LFO_wave[LFO_ENT];
/*TODO*///#endif    

}
