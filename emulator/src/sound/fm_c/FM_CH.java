package sound.fm_c;

public class FM_CH {

    public FM_CH() {
        SLOT = new FM_SLOT[4];
        for (int i = 0; i < 4; i++) {
            SLOT[i] = new FM_SLOT();
        }
    }
    public FM_SLOT[] SLOT;
    public int /*UINT8*/ PAN;			/* PAN :NONE,LEFT,RIGHT or CENTER */
    /*TODO*///	UINT8 ALGO;			/* Algorythm                      */
/*TODO*///	UINT8 FB;			/* shift count of self feed back  */
/*TODO*///	INT32 op1_out[2];	/* op1 output for beedback        */
/*TODO*///	/* Algorythm (connection) */
/*TODO*///	INT32 *connect1;		/* pointer of SLOT1 output    */
/*TODO*///	INT32 *connect2;		/* pointer of SLOT2 output    */
/*TODO*///	INT32 *connect3;		/* pointer of SLOT3 output    */
/*TODO*///	INT32 *connect4;		/* pointer of SLOT4 output    */
/*TODO*///	/* LFO */
/*TODO*///	INT32 pms;				/* PMS depth level of channel */
/*TODO*///	UINT32 ams;				/* AMS depth level of channel */
/*TODO*///	/* Phase Generator */

    public long /*UINT32*/ fc;			/* fnum,blk    :adjusted to sampling rate */
    /*TODO*///	UINT8 fn_h;			/* freq latch  :                   */
/*TODO*///	UINT8 kcode;		/* key code    :                   */    

}
