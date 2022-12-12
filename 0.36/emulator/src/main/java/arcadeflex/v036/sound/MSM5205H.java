/*
 * ported to v0.36
 * 
 */
package arcadeflex.v036.sound;

public class MSM5205H {

    public static abstract interface vclk_InterruptHandlerPtr {

        public abstract void handler(int num);
    }
    /* an interface for the MSM5205 and similar chips */

    public static int MAX_MSM5205 = 4;

    /* priscaler selector defines   */
 /* default master clock is 384KHz */
    public static int MSM5205_S96_3B = 0;
    /* prsicaler 1/96(4KHz) , data 3bit */

    public static int MSM5205_S48_3B = 1;
    /* prsicaler 1/48(8KHz) , data 3bit */

    public static int MSM5205_S64_3B = 2;
    /* prsicaler 1/64(6KHz) , data 3bit */

    public static int MSM5205_SEX_3B = 3;
    /* VCLK slave mode      , data 3bit */

    public static int MSM5205_S96_4B = 4;
    /* prsicaler 1/96(4KHz) , data 4bit */

    public static int MSM5205_S48_4B = 5;
    /* prsicaler 1/48(8KHz) , data 4bit */

    public static int MSM5205_S64_4B = 6;
    /* prsicaler 1/64(6KHz) , data 4bit */

    public static int MSM5205_SEX_4B = 7;

    /* VCLK slave mode      , data 4bit */


    public static class MSM5205interface {

        public MSM5205interface(int num, int baseclock, vclk_InterruptHandlerPtr[] vclk_interrupt, int[] select, int[] mixing_level) {
            this.num = num;
            this.baseclock = baseclock;
            this.vclk_interrupt = vclk_interrupt;
            this.select = select;
            this.mixing_level = mixing_level;
        }
        int num;
        /* total number of chips                 */
        int baseclock;
        /* master clock (default = 384KHz)       */
        vclk_InterruptHandlerPtr[] vclk_interrupt;    //void (*vclk_interrupt[MAX_MSM5205])(int);   /* VCLK interrupt callback  */
        int[] select;//[MAX_MSM5205];       /* prescaler / bit width selector        */
        int[] mixing_level;//[MAX_MSM5205]; /* master volume                         */
    }

}
