package sound;

public class nes_apuH {

    public static final int MAX_NESPSG = 2;

    /* AN EXPLANATION
     *
     * The NES APU is actually integrated into the Nintendo processor.
     * You must supply the same number of APUs as you do processors.
     * Also make sure to correspond the memory regions to those used in the
     * processor, as each is shared.
     */
    public static class NESinterface {

        public NESinterface(int num, int[] region, int[] volume) {
            this.num = num;
            this.region = region;
            this.volume = volume;
        }
        public int num;                 /* total number of chips in the machine */

        public int[] region;//[MAX_NESPSG];  /* DMC regions */
        public int[] volume;//[MAX_NESPSG];
    };
}
