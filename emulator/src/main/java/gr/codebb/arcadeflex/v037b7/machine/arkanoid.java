/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.machine;

import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;

public class arkanoid {

    public static int arkanoid_paddle_select;

    static int z80write, fromz80, m68705write, toz80;

    static /*unsigned*/ char u8_portA_in, u8_portA_out, u8_ddrA;
    static /*unsigned*/ char u8_portC_out, u8_ddrC;

    public static InitMachinePtr arkanoid_init_machine = new InitMachinePtr() {
        public void handler() {
            u8_portA_in = 0;
            u8_portA_out = 0;
            z80write = 0;
            m68705write = 0;
        }
    };

    public static ReadHandlerPtr arkanoid_Z80_mcu_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* return the last value the 68705 wrote, and mark that we've read it */
            m68705write = 0;
            return toz80;
        }
    };

    public static WriteHandlerPtr arkanoid_Z80_mcu_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* a write from the Z80 has occurred, mark it and remember the value */
            z80write = 1;
            fromz80 = data;

            /* give up a little bit of time to let the 68705 detect the write */
            cpu_spinuntil_trigger(700);
        }
    };

    public static ReadHandlerPtr arkanoid_68705_portA_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (u8_portA_out & u8_ddrA) | (u8_portA_in & ~u8_ddrA);
        }
    };

    public static WriteHandlerPtr arkanoid_68705_portA_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            u8_portA_out = (char) (data & 0xFF);
        }
    };

    public static WriteHandlerPtr arkanoid_68705_ddrA_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            u8_ddrA = (char) (data & 0xFF);
        }
    };

    public static ReadHandlerPtr arkanoid_68705_portC_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int res = 0;

            /* bit 0 is high on a write strobe; clear it once we've detected it */
            if (z80write != 0) {
                res |= 0x01;
            }

            /* bit 1 is high if the previous write has been read */
            if (m68705write == 0) {
                res |= 0x02;
            }

            return (u8_portC_out & u8_ddrC) | (res & ~u8_ddrC);
        }
    };

    public static WriteHandlerPtr arkanoid_68705_portC_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((u8_ddrC & 0x04) != 0 && (~data & 0x04) != 0 && (u8_portC_out & 0x04) != 0) {
                /* mark that the command has been seen */
                cpu_trigger.handler(700);

                /* return the last value the Z80 wrote */
                z80write = 0;
                u8_portA_in = (char) fromz80;
            }
            if ((u8_ddrC & 0x08) != 0 && (~data & 0x08) != 0 && (u8_portC_out & 0x08) != 0) {
                /* a write from the 68705 to the Z80; remember its value */
                m68705write = 1;
                toz80 = u8_portA_out;
            }

            u8_portC_out = (char) (data & 0xFF);
        }
    };

    public static WriteHandlerPtr arkanoid_68705_ddrC_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            u8_ddrC = (char) (data & 0xFF);
        }
    };

    public static ReadHandlerPtr arkanoid_68705_input_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int res = input_port_0_r.handler(offset) & 0x3f;

            /* bit 0x40 comes from the sticky bit */
            if (z80write == 0) {
                res |= 0x40;
            }

            /* bit 0x80 comes from a write latch */
            if (m68705write == 0) {
                res |= 0x80;
            }

            return res;
        }
    };

    public static ReadHandlerPtr arkanoid_input_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (arkanoid_paddle_select != 0) {
                return input_port_3_r.handler(offset);
            } else {
                return input_port_2_r.handler(offset);
            }
        }
    };

}
