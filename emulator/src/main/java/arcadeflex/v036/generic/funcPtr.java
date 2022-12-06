/*
 * ported to 0.36
 */
package arcadeflex.v036.generic;

public class funcPtr {

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
