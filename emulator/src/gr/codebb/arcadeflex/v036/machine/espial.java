/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.machine;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.cpu.z80.z80H.*;

public class espial {

    public static InitMachinePtr espial_init_machine = new InitMachinePtr() {
        public void handler() {
            /* we must start with NMI interrupts disabled */
            //interrupt_enable = 0;
            interrupt_enable_w.handler(0, 0);
        }
    };

    public static WriteHandlerPtr zodiac_master_interrupt_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            interrupt_enable_w.handler(offset, data ^ 1);
        }
    };

    public static InterruptPtr zodiac_master_interrupt = new InterruptPtr() {
        public int handler() {
            return (cpu_getiloops() == 0) ? nmi_interrupt.handler() : interrupt.handler();
        }
    };

    public static WriteHandlerPtr zodiac_master_soundlatch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(offset, data);
            cpu_cause_interrupt(1, Z80_IRQ_INT);
        }
    };

}
