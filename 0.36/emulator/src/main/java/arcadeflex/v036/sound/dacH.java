/*
 * ported to 0.37b7
 */
package arcadeflex.v036.sound;

public class dacH {

    public static final int MAX_DAC = 4;

    public static class DACinterface {

        public DACinterface(int num, int[] mixing_level) {
            this.num = num;
            this.mixing_level = mixing_level;
        }
        public int num;/* total number of DACs */
        public int mixing_level[];
    }
}
