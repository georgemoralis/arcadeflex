/*
 * ported to v0.36
 */
/**
 * Changelog
 * =========
 * 25/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.machine;

//cpu imports
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.inptport.*;
//vidhdrw imports
import static arcadeflex.v036.vidhrdw.avgdvg.*;
//TODO
import static gr.codebb.arcadeflex.v036.platform.input.*;
import static gr.codebb.arcadeflex.v036.sound.pokey.*;
import static gr.codebb.arcadeflex.v036.sound.pokeyH.*;

public class quantum {

    /**
     * * quantum_interrupt
     *
     * Purpose: do an interrupt - so many times per raster frame
     *
     * Returns: 0
     *
     * History: 11/19/97 PF Created
     *
     *************************
     */
    public static InterruptHandlerPtr quantum_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            return 1;
            /* ipl0' == ivector 1 */
        }
    };

    /**
     * * quantum_switches_r
     *
     * Purpose: read switches input, which sneaks the VHALT' in
     *
     * Returns: byte
     *
     * History: 11/20/97 PF Created
     *
     *************************
     */
    public static ReadHandlerPtr quantum_switches_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (input_port_0_r.handler(0)
                    | (avgdvg_done() != 0 ? 1 : 0));
        }
    };

    public static WriteHandlerPtr quantum_led_write = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0 and 1 are coin counters */
            coin_counter_w.handler(0, data & 2);
            coin_counter_w.handler(1, data & 1);

            /* bits 4 and 5 are LED controls */
            osd_led_w.handler(0, (data & 0x10) >> 4);
            osd_led_w.handler(1, (data & 0x20) >> 5);
            /* other bits unknown */
        }
    };

    /**
     * * quantum_snd_read, quantum_snd_write
     *
     * Purpose: read and write POKEY chips - need to do translation, so we don't
     * directly map it
     *
     * Returns: register value, for read
     *
     * History: 11/19/97 PF Created
     *
     *************************
     */
    public static WriteHandlerPtr quantum_snd_write = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((offset & 0x20) != 0) /* A5 selects chip */ {
                pokey2_w.handler((offset >> 1) % 0x10, data);
            } else {
                pokey1_w.handler(((offset >> 1) & 0xF) % 0x10, data);
            }
        }
    };

    public static ReadHandlerPtr quantum_snd_read = new ReadHandlerPtr() {
        public int handler(int offset) {
            if ((offset & 0x20) != 0) {
                return pokey2_r.handler((offset >> 1) % 0x10);
            } else {
                return pokey1_r.handler((offset >> 1) % 0x10);
            }
        }
    };

    /**
     * * quantum_trackball
     *
     * Purpose: read trackball port. So far, attempting theory: D0-D3 - vert
     * movement delta D4-D7 - horz movement delta
     *
     * if wrong, will need to pull out my 74* logic reference
     *
     * Returns: 8 bit value
     *
     * History: 11/19/97 PF Created
     *
     *************************
     */
    public static ReadHandlerPtr quantum_trackball_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int x, y;

            x = input_port_4_r.handler(offset);
            y = input_port_3_r.handler(offset);

            return (x << 4) + y;
        }
    };

    /**
     * * quantum_input_1_r, quantum_input_2_r
     *
     * Purpose: POKEY input switches read
     *
     * Returns: in the high bit the appropriate switch value
     *
     * History: 12/2/97 ASG Created
     *
     *************************
     */
    public static ReadHandlerPtr quantum_input_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (input_port_1_r.handler(0) << (7 - (offset - POT0_C))) & 0x80;
        }
    };

    public static ReadHandlerPtr quantum_input_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (input_port_2_r.handler(0) << (7 - (offset - POT0_C))) & 0x80;
        }
    };
}
