/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 */
package arcadeflex.v036.machine;

//cpu imports
import static arcadeflex.v036.cpu.z80.z80H.*;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.cpuintrf.*;
//common imports
import static common.libc.expressions.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class lwings {

    static int lwings_bank_register = 0xff;

    public static WriteHandlerPtr lwings_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU1);
            int bank = (data >> 1) & 0x3;
            cpu_setbank(1, new UBytePtr(RAM, 0x10000 + bank * 0x4000));

            lwings_bank_register = data;
        }
    };

    public static InterruptHandlerPtr lwings_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            return 0x00d7;
            /* RST 10h */
        }
    };
    static int n_aveng;
    static int s_aveng;
    public static InterruptHandlerPtr avengers_interrupt = new InterruptHandlerPtr() {
        public int handler() {/* hack */
            if ((lwings_bank_register & 0x08) != 0) {
                /* NMI enable */
                s_aveng = NOT(s_aveng);
                if (s_aveng != 0) {
                    return interrupt.handler();
                    //cpu_cause_interrupt(0, 0xd7);
                } else {
                    return Z80_NMI_INT;
                }
            }

            return Z80_IGNORE_INT;
        }
    };

}
