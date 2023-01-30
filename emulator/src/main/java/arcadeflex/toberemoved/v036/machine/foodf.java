/*
 * ported to v0.36
 * 
 */
package arcadeflex.toberemoved.v036.machine;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptport.*;
//common imports
import static common.libc.cstring.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;

public class foodf {

    static int whichport = 0;

    static TimerCallbackHandlerPtr foodf_delayed_interrupt = new TimerCallbackHandlerPtr() {
        @Override
        public void handler(int param) {
            cpu_cause_interrupt(0, 2);
        }
    };

    public static InterruptHandlerPtr foodf_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            /* INT 2 once per frame in addition to... */
            if (cpu_getiloops() == 0) {
                timer_set(TIME_IN_USEC(100), 0, foodf_delayed_interrupt);
            }

            /* INT 1 on the 32V signal */
            return 1;
        }
    };

    static UBytePtr nvram = new UBytePtr(256);

    public static ReadHandlerPtr foodf_nvram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return ((nvram.read((offset / 4) ^ 0x03) >> 2 * (offset % 4))) & 0x0f;
        }
    };

    public static WriteHandlerPtr foodf_nvram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            nvram.write((offset / 4) ^ 0x03, nvram.read((offset / 4) ^ 0x03) & ~(0x0f << 2 * (offset % 4)));
            nvram.write((offset / 4) ^ 0x03, nvram.read((offset / 4) ^ 0x03) | (data & 0x0f) << 2 * (offset % 4));
        }
    };

    public static nvramHandlerPtr foodf_nvram_handler = new nvramHandlerPtr() {
        public void handler(Object file, int read_or_write) {
            if (read_or_write != 0) {
                osd_fwrite(file, new UBytePtr(nvram), 128);
            } else {
                if (file != null) {
                    osd_fread(file, new UBytePtr(nvram), 128);
                } else {
                    memset(new UBytePtr(nvram), 0xff, 128);
                }
            }
        }
    };

    /*
	 *		Analog controller read dispatch.
     */
    public static ReadHandlerPtr foodf_analog_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (offset) {
                case 0:
                case 2:
                case 4:
                case 6:
                    return readinputport(whichport);
            }
            return 0;
        }
    };

    public static ReadHandlerPtr foodf_digital_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (offset) {
                case 0:
                    return input_port_4_r.handler(offset);
            }
            return 0;
        }
    };

    public static WriteHandlerPtr foodf_analog_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            whichport = 3 - ((offset / 2) & 3);
        }
    };

    public static WriteHandlerPtr foodf_digital_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
        }
    };
}
