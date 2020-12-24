package gr.codebb.arcadeflex.v036.sound.fm_c;

import gr.codebb.arcadeflex.common.SubArrays.IntSubArray;
import gr.codebb.arcadeflex.v036.sound.YM_DELTAT;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class YM2610 {

    public FM_OPN OPN;/* OPN state    */

    public FM_CH[] CH; /* channel state */

    public int address1;/* address register1 */

    public ADPCM_CH[] adpcm;				/* adpcm channels */


    public YM2610() {
        OPN = new FM_OPN();
        CH = new FM_CH[6];
        adpcm = new ADPCM_CH[7];
        for (int i = 0; i < 6; i++) {
            CH[i] = new FM_CH();
        }
        for (int i = 0; i < 7; i++) {
            adpcm[i] = new ADPCM_CH();
        }
        adpcmreg = new int[0x30];

    }
    /* ADPCM-A unit */
    public UBytePtr pcmbuf;			/* pcm rom buffer */

    public int pcm_size;			/* size of pcm rom */

    public IntSubArray adpcmTL;					/* adpcmA total level */

    public int[] adpcmreg;//	/* registers */
    public int adpcm_arrivedEndAddress;
    /* Delta-T ADPCM unit */
    public YM_DELTAT deltaT;

}
