package sound.fm_c;
import sound.YM_DELTAT;
import static arcadeflex.ptrlib.*;

public class YM2610 {

    public FM_OPN OPN;/* OPN state    */
    public FM_CH[] CH; /* channel state */
    public int address1;/* address register1 */
    public YM2610() {
        OPN = new FM_OPN();
        CH = new FM_CH[6];
        for (int i = 0; i < 6; i++) {
            CH[i] = new FM_CH();
        }
    }
	/* ADPCM-A unit */
	public UBytePtr pcmbuf;			/* pcm rom buffer */
	public int pcm_size;			/* size of pcm rom */
/*TODO*///	INT32 *adpcmTL;					/* adpcmA total level */
/*TODO*///	ADPCM_CH adpcm[6];				/* adpcm channels */
/*TODO*///	UINT32 adpcmreg[0x30];	/* registers */
	public int adpcm_arrivedEndAddress;
	/* Delta-T ADPCM unit */
    public YM_DELTAT deltaT;

}
