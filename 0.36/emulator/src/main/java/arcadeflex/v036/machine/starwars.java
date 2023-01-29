/*
 * ported to v0.36
 */
/**
 * Changelog
 * =========
 * 29/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.machine;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptport.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.avgdvg.*;

public class starwars {

    /* control select values for ADC_R */
    public static final int kPitch = 0;
    public static final int kYaw = 1;
    public static final int kThrust = 2;

    static int control_num = kPitch;
    /**
     * *****************************************************
     */
    public static ReadHandlerPtr starwars_input_bank_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int x;
            x = input_port_1_r.handler(0);
            /* Read memory mapped port 2 */

 /* Kludge to enable Starwars Mathbox Self-test                  */
 /* The mathbox looks like it's running, from this address... :) */
            if (cpu_get_pc() == 0xf978) {
                x |= 0x80;
            }

            /* Kludge to enable Empire Mathbox Self-test                  */
 /* The mathbox looks like it's running, from this address... :) */
            if (cpu_get_pc() == 0xf655) {
                x |= 0x80;
            }

            if (avgdvg_done() != 0) {
                x |= 0x40;
            } else {
                x &= ~0x40;
            }

            return x;
        }
    };
    /**
     * ******************************************************
     */
    /**
     * *****************************************************
     */
    public static ReadHandlerPtr starwars_control_r = new ReadHandlerPtr() {
        public int handler(int offset) {

            if (control_num == kPitch) {
                return readinputport(4);
            } else if (control_num == kYaw) {
                return readinputport(5);
            } /* default to unused thrust */ else {
                return 0;
            }
        }
    };

    public static WriteHandlerPtr starwars_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            control_num = offset;
        }
    };

}
