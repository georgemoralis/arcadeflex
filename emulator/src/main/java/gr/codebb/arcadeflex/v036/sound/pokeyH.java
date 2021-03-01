package gr.codebb.arcadeflex.v036.sound;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;

public class pokeyH {
    /* POKEY WRITE LOGICALS */

    public static final int AUDF1_C = 0x00;
    public static final int AUDC1_C = 0x01;
    public static final int AUDF2_C = 0x02;
    public static final int AUDC2_C = 0x03;
    public static final int AUDF3_C = 0x04;
    public static final int AUDC3_C = 0x05;
    public static final int AUDF4_C = 0x06;
    public static final int AUDC4_C = 0x07;
    public static final int AUDCTL_C = 0x08;
    public static final int STIMER_C = 0x09;
    public static final int SKREST_C = 0x0A;
    public static final int POTGO_C = 0x0B;
    public static final int SEROUT_C = 0x0D;
    public static final int IRQEN_C = 0x0E;
    public static final int SKCTL_C = 0x0F;

    /* POKEY READ LOGICALS */
    public static final int POT0_C = 0x00;
    public static final int POT1_C = 0x01;
    public static final int POT2_C = 0x02;
    public static final int POT3_C = 0x03;
    public static final int POT4_C = 0x04;
    public static final int POT5_C = 0x05;
    public static final int POT6_C = 0x06;
    public static final int POT7_C = 0x07;
    public static final int ALLPOT_C = 0x08;
    public static final int KBCODE_C = 0x09;
    public static final int RANDOM_C = 0x0A;
    public static final int SERIN_C = 0x0D;
    public static final int IRQST_C = 0x0E;
    public static final int SKSTAT_C = 0x0F;

    /* exact 1.79 MHz clock freq (of the Atari 800 that is) */
    public static final int FREQ_17_EXACT = 1789790;

    /*
     * We can now handle the exact frequency as well as any other,
     * because aliasing effects are suppressed for pure tones.
     */
    public static final int FREQ_17_APPROX = FREQ_17_EXACT;

    public static final int MAXPOKEYS = 4;	/* max number of emulated chips */


    /**
     * ***************************************************************************
     * pot0_r to pot7_r: Handlers for reading the pot values. Some Atari games
     * use ALLPOT to return dipswitch settings and other things. serin_r,
     * serout_w, interrupt_cb: New function pointers for serial input/output and
     * a interrupt callback.
 ****************************************************************************
     */
    public static abstract interface interrupt_cbPtr {

        public abstract void handler(int mask);
    }

    public static class POKEYinterface {

        public POKEYinterface(int num,int baseclock,int[] mixing_level,
                ReadHandlerPtr[] pot0_r,
                ReadHandlerPtr[] pot1_r,
                ReadHandlerPtr[] pot2_r,
                ReadHandlerPtr[] pot3_r,
                ReadHandlerPtr[] pot4_r,
                ReadHandlerPtr[] pot5_r,
                ReadHandlerPtr[] pot6_r,
                ReadHandlerPtr[] pot7_r,
                ReadHandlerPtr[] allpot_r,
                ReadHandlerPtr[] serin_r,
                WriteHandlerPtr[] serout_w,
                interrupt_cbPtr[] interrupt_cb
                )
        {
            this.num=num;
            this.baseclock=baseclock;
            this.mixing_level=mixing_level;
            this.pot0_r=pot0_r;
            this.pot1_r=pot1_r;
            this.pot2_r=pot2_r;
            this.pot3_r=pot3_r;
            this.pot4_r=pot4_r;
            this.pot5_r=pot5_r;
            this.pot6_r=pot6_r;
            this.pot7_r=pot7_r;
            this.allpot_r=allpot_r;
            this.serin_r=serin_r;
            this.serout_w=serout_w;
            this.interrupt_cb=interrupt_cb;
        }
        public POKEYinterface(int num,int baseclock,int[] mixing_level,
                ReadHandlerPtr[] pot0_r,
                ReadHandlerPtr[] pot1_r,
                ReadHandlerPtr[] pot2_r,
                ReadHandlerPtr[] pot3_r,
                ReadHandlerPtr[] pot4_r,
                ReadHandlerPtr[] pot5_r,
                ReadHandlerPtr[] pot6_r,
                ReadHandlerPtr[] pot7_r,
                ReadHandlerPtr[] allpot_r                
                )
        {
            this.num=num;
            this.baseclock=baseclock;
            this.mixing_level=mixing_level;
            this.pot0_r=pot0_r;
            this.pot1_r=pot1_r;
            this.pot2_r=pot2_r;
            this.pot3_r=pot3_r;
            this.pot4_r=pot4_r;
            this.pot5_r=pot5_r;
            this.pot6_r=pot6_r;
            this.pot7_r=pot7_r;
            this.allpot_r=allpot_r;
            this.serin_r=null;
            this.serout_w=null;
            this.interrupt_cb=null;
        }
        
        public POKEYinterface(int num,int baseclock,int[] mixing_level){
            this.num=num;
            this.baseclock=baseclock;
            this.mixing_level=mixing_level;
            
            pot0_r = new ReadHandlerPtr[num];
            pot1_r = new ReadHandlerPtr[num];
            pot2_r = new ReadHandlerPtr[num];
            pot3_r = new ReadHandlerPtr[num];
            pot4_r = new ReadHandlerPtr[num];
            pot5_r = new ReadHandlerPtr[num];
            pot6_r = new ReadHandlerPtr[num];
            pot7_r = new ReadHandlerPtr[num];
            
            allpot_r = new ReadHandlerPtr[num];
            serin_r = new ReadHandlerPtr[num];
            serout_w = new WriteHandlerPtr[num];
            interrupt_cb = new interrupt_cbPtr[num];
            
            for (int _i=0;_i<num;_i++){
                pot0_r[_i]=null;
                pot1_r[_i]=null;
                pot2_r[_i]=null;
                pot3_r[_i]=null;
                pot4_r[_i]=null;
                pot5_r[_i]=null;
                pot6_r[_i]=null;
                pot7_r[_i]=null;
                allpot_r[_i]=null;
                serin_r[_i]=null;
                serout_w[_i]=null;
                interrupt_cb[_i]=null;
            }
        }
        
        int num;    /* total number of pokeys in the machine */
        int baseclock;
        int[] mixing_level;//[MAXPOKEYS];
        ReadHandlerPtr[] pot0_r;//int (*pot0_r[MAXPOKEYS])(int offset);
        ReadHandlerPtr[] pot1_r;//int (*pot1_r[MAXPOKEYS])(int offset);
        ReadHandlerPtr[] pot2_r;//int (*pot2_r[MAXPOKEYS])(int offset);
        ReadHandlerPtr[] pot3_r;//int (*pot3_r[MAXPOKEYS])(int offset);
        ReadHandlerPtr[] pot4_r;//int (*pot4_r[MAXPOKEYS])(int offset);
        ReadHandlerPtr[] pot5_r;//int (*pot5_r[MAXPOKEYS])(int offset);
        ReadHandlerPtr[] pot6_r;//int (*pot6_r[MAXPOKEYS])(int offset);
        ReadHandlerPtr[] pot7_r;//int (*pot7_r[MAXPOKEYS])(int offset);
        ReadHandlerPtr[] allpot_r;//int (*allpot_r[MAXPOKEYS])(int offset);
        ReadHandlerPtr[] serin_r;//int (*serin_r[MAXPOKEYS])(int offset);
        WriteHandlerPtr[] serout_w;//void (*serout_w[MAXPOKEYS])(int offset, int data);
        interrupt_cbPtr[] interrupt_cb;//void (*interrupt_cb[MAXPOKEYS])(int mask);
    };
}
