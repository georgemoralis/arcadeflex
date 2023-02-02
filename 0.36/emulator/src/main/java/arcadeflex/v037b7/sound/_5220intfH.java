/*
 * ported to v0.37b7
 *
 */
package arcadeflex.v037b7.sound;

public class _5220intfH {

    public static abstract interface IrqPtr {

        public abstract void handler(int state);
    }

    public static class TMS5220interface {

        public TMS5220interface(int baseclock, int mixing_level, IrqPtr irq) {
            this.baseclock = baseclock;
            this.mixing_level = mixing_level;
            this.irq = irq;
        }

        int baseclock;
        /* clock rate = 80 * output sample rate,     */
 /* usually 640000 for 8000 Hz sample rate or */
 /* usually 800000 for 10000 Hz sample rate.  */
        int mixing_level;
        IrqPtr irq;/* IRQ callback function */
    }
}
