package sound;

import static sound.mixerH.*;
import static mame.driverH.*;

public class _2151intfH {

    public static final int MAX_2151 = 3;

    public static final int YM3012_VOL(int LVol, int LPan, int RVol, int RPan) {
        return (MIXER(LVol, LPan) | (MIXER(RVol, RPan) << 16));
    }

    public static class YM2151interface {

        public YM2151interface(int num, int baseclock, int[] volume, WriteYmHandlerPtr[] irqhandler, WriteHandlerPtr[] portwritehandler) {
            this.num = num;
            this.baseclock = baseclock;
            this.volume = volume;
            this.irqhandler = irqhandler;
            this.portwritehandler = portwritehandler;

        }
        public YM2151interface(int num, int baseclock, int[] volume, WriteYmHandlerPtr[] irqhandler) {
            this.num = num;
            this.baseclock = baseclock;
            this.volume = volume;
            this.irqhandler = irqhandler;
            this.portwritehandler = new WriteHandlerPtr[num];
            for(int i=0; i<num; i++)
            {
             this.portwritehandler[i] = null;
            }
        }
        int num;
        int baseclock;
        int[] volume;//[MAX_2151]; /* need for use YM3012()_VOL macro */
        WriteYmHandlerPtr[] irqhandler;//[MAX_2151])(int irq);
        WriteHandlerPtr[] portwritehandler;//void (*portwritehandler[MAX_2151])(int,int);
    };
}
