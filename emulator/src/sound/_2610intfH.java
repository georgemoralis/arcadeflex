package sound;

import static mame.driverH.*;
import static sound.ay8910H.*;
import static sound.mixerH.*;

public class _2610intfH {

    public static final int MAX_2610 = (2);

    public static class YM2610interface extends AY8910interface {

        public YM2610interface(int num, int baseclock, int[] mixing_level, ReadHandlerPtr[] pAr, ReadHandlerPtr[] pBr, WriteHandlerPtr[] pAw, WriteHandlerPtr[] pBw, WriteYmHandlerPtr[] ym_handler, int[] pcmromb, int[] pcmroma, int[] volumeFM) {
            super(num, baseclock, mixing_level, pAr, pBr, pAw, pBw, ym_handler);
            this.pcmromb = pcmromb;
            this.pcmroma = pcmroma;
            this.volumeFM = volumeFM;
        }
        int[] pcmromb;		/* Delta-T rom region */

        int[] pcmroma;		/* ADPCM   rom region */

        int[] volumeFM;		/* use YM3012_VOL macro */

    }

}
