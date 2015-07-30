package sound.fm_c;

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
    /*TODO*///typedef struct ym2610_f {
/*TODO*///	FM_OPN OPN;				
/*TODO*///	FM_CH CH[6];			

/*TODO*///	/* ADPCM-A unit */
/*TODO*///	UINT8 *pcmbuf;			/* pcm rom buffer */
/*TODO*///	UINT32 pcm_size;			/* size of pcm rom */
/*TODO*///	INT32 *adpcmTL;					/* adpcmA total level */
/*TODO*///	ADPCM_CH adpcm[6];				/* adpcm channels */
/*TODO*///	UINT32 adpcmreg[0x30];	/* registers */
/*TODO*///	UINT8 adpcm_arrivedEndAddress;
/*TODO*///	/* Delta-T ADPCM unit */
/*TODO*///	YM_DELTAT deltaT;
/*TODO*///} YM2610;
}
