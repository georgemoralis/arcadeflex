package sound.fm_c;

import static platform.libc.*;

public class FM_CH {

    public FM_CH() {
        SLOT = new FM_SLOT[4];
        for (int i = 0; i < 4; i++) {
            SLOT[i] = new FM_SLOT();
        }
        op1_out = new int[2];
    }
    public FM_SLOT[] SLOT;
    public int /*UINT8*/ PAN;			/* PAN :NONE,LEFT,RIGHT or CENTER */

    public int /*UINT8*/ ALGO;			/* Algorythm                      */

    public int /*UINT8*/ FB;			/* shift count of self feed back  */

    public int[] op1_out;	/* op1 output for beedback        */
    /* Algorythm (connection) */

    public IntSubArray connect1;		/* pointer of SLOT1 output    */

    public IntSubArray connect2;		/* pointer of SLOT2 output    */

    public IntSubArray connect3;		/* pointer of SLOT3 output    */

    public IntSubArray connect4;		/* pointer of SLOT4 output    */
    /* LFO */

    public int pms;				/* PMS depth level of channel */

    public long /*UINT32*/ ams;				/* AMS depth level of channel */
    /* Phase Generator */

    public long /*UINT32*/ fc;			/* fnum,blk    :adjusted to sampling rate */

    public int /*UINT8*/ fn_h;			/* freq latch  :                   */

    public int /*UINT8*/ kcode;		/* key code    :                   */

}
