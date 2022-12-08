/*
 * ported to v0.36
 *
 */
package arcadeflex.v036.sound;

import gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intfH.MAX_3812;
import gr.codebb.arcadeflex.v037b7.sound._3812intfH.YM3812interface;

public class _2413intfH {

    public static final int MAX_2413 = MAX_3812;

    public static class YM2413interface extends YM3812interface {

        public YM2413interface(int num, int baseclock, int[] mixing_level, WriteYmHandlerPtr[] handler) {
            super(num, baseclock, mixing_level, handler);
        }

        public YM2413interface(int num, int baseclock, int[] mixing_level) {
            super(num, baseclock, mixing_level);
        }
    }
}
