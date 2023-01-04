/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 */
/**
 * Changelog
 * =========
 * 04/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.sndhrdw;

//cpu imports
import static arcadeflex.v036.cpu.i8039.i8039H.*;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrf.*;
//sound imports
import static arcadeflex.v036.sound.dac.*;

public class spacefb {

    static /*unsigned*/ char spacefb_sound_latch;

    public static ReadHandlerPtr spacefb_sh_getp2 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return ((spacefb_sound_latch & 0x18) << 1);
        }
    };

    public static ReadHandlerPtr spacefb_sh_gett0 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return spacefb_sound_latch & 0x20;
        }
    };

    public static ReadHandlerPtr spacefb_sh_gett1 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return spacefb_sound_latch & 0x04;
        }
    };

    public static WriteHandlerPtr spacefb_port_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            spacefb_sound_latch = (char) (data & 0xFF);
            if ((data & 0x02) == 0) {
                cpu_cause_interrupt(1, I8039_EXT_INT);
            }
        }
    };

    public static WriteHandlerPtr spacefb_sh_putp1 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            DAC_data_w.handler(0, data);
        }
    };

}
