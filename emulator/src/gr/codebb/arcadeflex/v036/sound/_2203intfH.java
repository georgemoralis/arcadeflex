package gr.codebb.arcadeflex.v036.sound;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.sound.ay8910H.*;

public class _2203intfH {

    public static final int MAX_2203 = 4;

    /* volume level for YM2203 */
    public static int YM2203_VOL(int FM_VOLUME, int SSG_VOLUME) {
        return (((FM_VOLUME) << 16) + (SSG_VOLUME));
    }

    public static class YM2203interface extends AY8910interface {

        public YM2203interface(int num, int baseclock, int[] mixing_level, ReadHandlerPtr[] pAr, ReadHandlerPtr[] pBr, WriteHandlerPtr[] pAw, WriteHandlerPtr[] pBw, WriteYmHandlerPtr[] ym_handler) {
            super(num, baseclock, mixing_level, pAr, pBr, pAw, pBw, ym_handler);
        }

        public YM2203interface(int num, int baseclock, int[] mixing_level, ReadHandlerPtr[] pAr, ReadHandlerPtr[] pBr, WriteHandlerPtr[] pAw, WriteHandlerPtr[] pBw) {
            super(num, baseclock, mixing_level, pAr, pBr, pAw, pBw);
        }

    }
}
