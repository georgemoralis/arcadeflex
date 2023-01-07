/*
 * ported to v0.36
 * using automatic conversion tool v0.09
 */
/**
 * Changelog
 * =========
 * 07/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.machine;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrf.*;

public class tp84 {

    public static ReadHandlerPtr tp84_beam_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            //	return cpu_getscanline();
            return 255;
            /* always return beam position 255 */ /* JB 970829 */


        }
    };
    public static WriteHandlerPtr tp84_catchloop_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (cpu_get_pc() == 0xe0f2) {
                cpu_spinuntil_int();
            }
        }
    };
}
