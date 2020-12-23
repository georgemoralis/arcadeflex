package gr.codebb.arcadeflex.v036.sound.fm_c;

import gr.codebb.arcadeflex.common.SubArrays.IntSubArray;
import static gr.codebb.arcadeflex.v036.sound.fmH.*;

public class FM_ST {

    public FM_ST() {
        DT_TABLE = new int[8][];
        for (int i = 0; i < 8; i++) {
            DT_TABLE[i] = new int[32];
        }
        AR_TABLE = new IntSubArray(94);
        DR_TABLE = new IntSubArray(94);

    }
    public int /*UINT8*/ index;		/* chip index (number of chip) */

    public int clock;			/* master clock  (Hz)  */

    public int rate;			/* sampling rate (Hz)  */

    public double freqbase;	/* frequency base      */

    public double TimerBase;	/* Timer base time     */

    public int /*UINT8*/ address;		/* address register    */

    public int /*UINT8*/ irq;			/* interrupt level     */

    public int /*UINT8*/ irqmask;		/* irq mask            */

    public int /*UINT8*/ status;		/* status flag         */

    public long /*UINT32*/ mode;		/* mode  CSM / 3SLOT   */

    public int TA;				/* timer a             */

    public int TAC;			/* timer a counter     */

    public int /*UINT8*/ TB;			/* timer b             */

    public int TBC;			/* timer b counter     */
    /* speedup customize */
    /* local time tables */

    public int[][] DT_TABLE;/* DeTune tables       */

    public IntSubArray AR_TABLE;		/* Atttack rate tables */

    public IntSubArray DR_TABLE;		/* Decay rate tables   */

    /* Extention Timer and IRQ handler */

    public FM_TIMERHANDLERtr Timer_Handler;
    public FM_IRQHANDLEPtr IRQ_Handler;
    /* timer model single / interval */
    public int /*UINT8*/ timermodel;
}
