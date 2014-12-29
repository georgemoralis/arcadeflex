package sound.fm_c;

import static sound.fm.*;

public class FM_OPN {

    public FM_OPN() {
        ST = new FM_ST();
        SL3 = new FM_3SLOT();
        LFO_wave = new int[LFO_ENT];
        FN_TABLE = new long[2048];
        LFO_FREQ=new long[8];
    }
    public int /*UINT8*/ type;		/* chip type         */

    public FM_ST ST;				/* general state     */
    public FM_3SLOT SL3;			/* 3 slot mode state */

    public FM_CH[] P_CH;			/* pointer of CH     */
    public long[] /*UINT32*/ FN_TABLE; /* fnumber -> increment counter */

    /*TODO*///	/* LFO */
/*TODO*///	UINT32 LFOCnt;
public long /*UINT32*/ LFOIncr;
    public long[] /*UINT32*/ LFO_FREQ;/* LFO FREQ table */
    public int[] LFO_wave;

}
