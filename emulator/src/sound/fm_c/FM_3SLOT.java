package sound.fm_c;

public class FM_3SLOT {

    public FM_3SLOT() {
        fc = new long[3];
        fn_h = new int[3];
        kcode = new int[3];
    }
    public long[] /*UINT32*/ fc;		/* fnum3,blk3  :calcrated */

    public int[] /*UINT8*/ fn_h;		/* freq3 latch            */

    public int[] /*UINT8*/ kcode;		/* key code    :          */

}
