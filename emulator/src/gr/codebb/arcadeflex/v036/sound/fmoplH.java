/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.codebb.arcadeflex.v036.sound;

/**
 *
 * @author shadow
 */
public class fmoplH {
         public static abstract interface OPL_TIMERHANDLERPtr { public abstract void handler(int channel,double interval_Sec); }
         public static abstract interface OPL_IRQHANDLERPtr { public abstract void handler(int param,int irq); }
         public static abstract interface OPL_UPDATEHANDLERPtr { public abstract void handler(int param,int min_interval_us); }
         public static abstract interface OPL_PORTHANDLER_WPtr { public abstract void handler(int param,/*unsigned char*/int data); }
         public static abstract interface OPL_PORTHANDLER_RPtr { public abstract char handler(int param); }

    /* !!!!! here is private section , do not access there member direct !!!!! */
    public static final int OPL_TYPE_WAVESEL   =0x01;  /* waveform select    */
    public static final int OPL_TYPE_ADPCM     =0x02; /* DELTA-T ADPCM unit */
    public static final int OPL_TYPE_KEYBOARD  =0x04;  /* keyboard interface */
    public static final int OPL_TYPE_IO        =0x08;  /* I/O port */

    /* ---------- Generic interface section ---------- */
    public static final int OPL_TYPE_YM3526= (0);
    public static final int OPL_TYPE_YM3812= (OPL_TYPE_WAVESEL);
    public static final int OPL_TYPE_Y8950 = (OPL_TYPE_ADPCM|OPL_TYPE_KEYBOARD|OPL_TYPE_IO);   
}
