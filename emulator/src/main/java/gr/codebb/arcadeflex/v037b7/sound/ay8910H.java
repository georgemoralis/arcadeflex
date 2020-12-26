/*
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.v037b7.sound;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;

public class ay8910H {

    public static final int MAX_8910 = 5;
    public static final int ALL_8910_CHANNELS = -1;

    public static class AY8910interface {

        public AY8910interface(int num, int baseclock, int[] mixing_level, ReadHandlerPtr[] pAr, ReadHandlerPtr[] pBr, WriteHandlerPtr[] pAw, WriteHandlerPtr[] pBw, WriteYmHandlerPtr[] ym_handler) {
            this.num = num;
            this.baseclock = baseclock;
            this.mixing_level = mixing_level;
            this.portAread = pAr;
            this.portBread = pBr;
            this.portAwrite = pAw;
            this.portBwrite = pBw;
            this.YM2203_handler = ym_handler;
        }

        //without ym2203 handler
        public AY8910interface(int num, int baseclock, int[] mixing_level, ReadHandlerPtr[] pAr, ReadHandlerPtr[] pBr, WriteHandlerPtr[] pAw, WriteHandlerPtr[] pBw) {
            this.num = num;
            this.baseclock = baseclock;
            this.mixing_level = mixing_level;
            this.portAread = pAr;
            this.portBread = pBr;
            this.portAwrite = pAw;
            this.portBwrite = pBw;
            this.YM2203_handler = null;
        }

        public int num;
        /* total number of 8910 in the machine_old */
        public int baseclock;
        public int[] mixing_level;              // int mixing_level[MAX_8910];
        public ReadHandlerPtr portAread[];      // int (*portAread[MAX_8910])(int offset);
        public ReadHandlerPtr portBread[];      // int (*portBread[MAX_8910])(int offset);
        public WriteHandlerPtr portAwrite[];    // void (*portAwrite[MAX_8910])(int offset,int data);
        public WriteHandlerPtr portBwrite[];    //void (*portBwrite[MAX_8910])(int offset,int data);
        public WriteYmHandlerPtr YM2203_handler[]; //void (*handler[MAX_8910])(int irq);	/* IRQ handler for the YM2203 */
    };
}
