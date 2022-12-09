/*
 * ported to 0.36
 */
package arcadeflex.v036.generic;

import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class funcPtr {

    /**
     * Common handlers
     */
    public static abstract interface ReadHandlerPtr {

        public abstract int handler(int offset);
    }

    public static abstract interface WriteHandlerPtr {

        public abstract void handler(int offset, int data);
    }

    public static abstract interface InitMachineHandlerPtr {

        public abstract void handler();
    }

    public static abstract interface InitDriverHandlerPtr {

        public abstract void handler();
    }

    public static abstract interface InterruptHandlerPtr {

        public abstract int handler();
    }

    public static abstract interface RomLoadHandlerPtr {

        public abstract void handler();
    }

    /**
     * Video related
     */
    public static abstract interface VhConvertColorPromHandlerPtr {

        public abstract void handler(char[] palette, char[] colortable, UBytePtr color_prom);
    }

    public static abstract interface VhStartHandlerPtr {

        public abstract int handler();
    }

    public static abstract interface VhStopHandlerPtr {

        public abstract void handler();
    }

    public static abstract interface VhUpdateHandlerPtr {

        public abstract void handler(osd_bitmap bitmap, int full_refresh);
    }

    /**
     * Sound related
     */
    public static abstract interface ShStartHandlerPtr {

        public abstract int handler(MachineSound msound);
    }

    public static abstract interface ShStopHandlerPtr {

        public abstract void handler();
    }

    public static abstract interface ShUpdateHandlerPtr {

        public abstract void handler();
    }

    /**
     * Timer callback
     */
    public static abstract interface TimerCallbackHandlerPtr {

        public abstract void handler(int i);
    }

    /**
     * cpu interface related
     */
    public static abstract interface BurnHandlerPtr {

        public abstract void handler(int cycles);
    }

    public static abstract interface IrqCallbackHandlerPtr {

        public abstract int handler(int irqline);
    }

    /**
     * Daisy chain related
     */
    public static abstract interface DaisyChainInterruptEntryPtr {

        public abstract int handler(int i);
    }

    public static abstract interface DaisyChainResetPtr {

        public abstract void handler(int i);
    }

    public static abstract interface DaisyChainInterruptRetiPtr {

        public abstract void handler(int i);
    }
}
