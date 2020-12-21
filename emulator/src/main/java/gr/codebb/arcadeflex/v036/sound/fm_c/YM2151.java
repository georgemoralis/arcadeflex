package gr.codebb.arcadeflex.v036.sound.fm_c;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;

public class YM2151 {

    public YM2151() {
        ST = new FM_ST();
        CH = new FM_CH[8];
        for (int i = 0; i < 8; i++) {
            CH[i] = new FM_CH();
        }
        KC_TABLE = new long[8 * 12 * 64 + 950];
    }
    public FM_ST ST;					/* general state     */

    public FM_CH CH[];				/* channel state     */

    public/*UINT8*/ int ct;					/* CT0,1             */

    public/*UINT32*/ long NoiseCnt;			/* noise generator   */

    public/*UINT32*/ long NoiseIncr;			/* noise mode enable & step */
    /*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	/* LFO */
/*TODO*///	UINT32 LFOCnt;
/*TODO*///	UINT32 LFOIncr;
/*TODO*///	UINT8 pmd;					/* LFO pmd level     */
/*TODO*///	UINT8 amd;					/* LFO amd level     */
/*TODO*///	INT32 *wavetype;			/* LFO waveform      */
/*TODO*///	INT32 LFO_wave[LFO_ENT*4];	/* LFO wave tabel    */
/*TODO*///	UINT8 testreg;				/* test register (LFO reset) */
/*TODO*///#endif

    public long[]/*UINT32*/ KC_TABLE;/* keycode,keyfunction -> count */

    public WriteHandlerPtr PortWrite;//void (*PortWrite)(int offset,int data);/*  callback when write CT0/CT1 */    
}
