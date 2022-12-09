/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.sndhrdw;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.driverH.*;


public class jedi {

    /* Misc sound code */
    static /*unsigned*/ char speech_write_buffer;

    public static WriteHandlerPtr jedi_speech_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if (offset < 0xff) {
                speech_write_buffer = (char) (data & 0xFF);
            } else if (offset < 0x1ff) {
/*TODO*///                tms5220_data_w.handler(0, speech_write_buffer);
            }
        }
    };

    public static ReadHandlerPtr jedi_speech_ready_r = new ReadHandlerPtr() {
        public int handler(int offset) {
/*TODO*///            return (NOT(tms5220_ready_r())) << 7;
            return 0;
        }
    };
}
