/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.sound;

import gr.codebb.arcadeflex.v036.mame.driverH.*;
import gr.codebb.arcadeflex.v037b7.sound.ay8910H.AY8910interface;

public class _2608intfH {

    public static final int MAX_2608 = (2);

    public static class YM2608interface extends AY8910interface {

        public YM2608interface(int num, int baseclock, int[] mixing_level, ReadHandlerPtr[] pAr, ReadHandlerPtr[] pBr, WriteHandlerPtr[] pAw, WriteHandlerPtr[] pBw, WriteYmHandlerPtr[] ym_handler, int[] pcmrom, int[] volumeFM) {
            super(num, baseclock, mixing_level, pAr, pBr, pAw, pBw, ym_handler);
            this.pcmrom = pcmrom;
            this.volumeFM = volumeFM;
        }

        public int[] pcmrom;/* Delta-T memory region ram/rom */
        public int[] volumeFM;/* use YM3012_VOL macro */
    };

}
