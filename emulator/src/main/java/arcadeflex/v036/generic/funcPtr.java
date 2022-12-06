/*
 * ported to 0.36
 */
package arcadeflex.v036.generic;

import static arcadeflex.v036.mame.sndintrfH.*;

public class funcPtr {

    /**
     * Sound related
     */
    /*public static abstract interface ShInitHandlerPtr {

        public abstract int handler(String gamename);
    }*/

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
