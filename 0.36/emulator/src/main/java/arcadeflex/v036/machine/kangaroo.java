/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */
package arcadeflex.v036.machine;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;

public class kangaroo {

    static int kangaroo_clock = 0;

    /* I have no idea what the security chip is nor whether it really does,
     this just seems to do the trick -V-
     */
    public static ReadHandlerPtr kangaroo_sec_chip_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /*  kangaroo_clock = (kangaroo_clock << 1) + 1; */
            kangaroo_clock++;
            return (kangaroo_clock & 0x0f);
        }
    };

    public static WriteHandlerPtr kangaroo_sec_chip_w = new WriteHandlerPtr() {
        public void handler(int offset, int val) {
            /*  kangaroo_clock = val & 0x0f; */
        }
    };
}
