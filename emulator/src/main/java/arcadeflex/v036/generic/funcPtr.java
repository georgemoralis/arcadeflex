/*
 * ported to 0.36
 */
package arcadeflex.v036.generic;

public class funcPtr {

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
