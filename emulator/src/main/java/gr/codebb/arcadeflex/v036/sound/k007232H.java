package gr.codebb.arcadeflex.v036.sound;

public class k007232H {

    public static final int MAX_K007232 = 3;

    public static int K007232_VOL(int LVol, int LPan, int RVol, int RPan) {
        return ((LVol) | ((LPan) << 8) | ((RVol) << 16) | ((RPan) << 24));
    }

    public static abstract interface portwritehandlerPtr {

        public abstract void handler(int a);
    }

    public static class K007232_interface {

        public K007232_interface(int num_chips, int[] bank, int[] volume, portwritehandlerPtr[] portwritehandler) {
            this.num_chips = num_chips;
            this.bank = bank;
            this.volume = volume;
            this.portwritehandler = portwritehandler;
        }

        int num_chips;			/* Number of chips */

        public int[] bank;//[MAX_K007232];	/* memory regions */
        int[] volume;//[MAX_K007232];/* volume */
        portwritehandlerPtr[] portwritehandler;//void (*portwritehandler[MAX_K007232])(int);
    };
}
