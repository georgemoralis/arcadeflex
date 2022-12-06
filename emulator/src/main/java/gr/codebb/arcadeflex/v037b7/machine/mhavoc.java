package gr.codebb.arcadeflex.v037b7.machine;

import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v037b7.cpu.m6502.m6502H.M6502_INT_NMI;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.avgdvg.*;

public class mhavoc {

    static int gamma_data;
    static int alpha_data;
    static int alpha_rcvd;
    static int alpha_xmtd;
    static int gamma_rcvd;
    static int gamma_xmtd;

    static int bank_select;
    static int player_1;

    static final int LS161_CLOCK = 2 * 5000;

    static Object gamma_timer = null;

    public static WriteHandlerPtr mhavoc_ram_banksel_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bank[] = {0x20200, 0x20800};
            UBytePtr RAM = memory_region(REGION_CPU1);

            data &= 0x01;
            logerror("Alpha RAM select: %02x\n", data);
            cpu_setbank(1, new UBytePtr(RAM, bank[data]));
        }
    };

    public static WriteHandlerPtr mhavoc_rom_banksel_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bank[] = {0x10000, 0x12000, 0x14000, 0x16000};
            UBytePtr RAM = memory_region(REGION_CPU1);

            data &= 0x03;

            logerror("Alpha ROM select: %02x\n", data);
            cpu_setbank(2, new UBytePtr(RAM, bank[data]));
        }
    };

    public static InitMachinePtr mhavoc_init_machine = new InitMachinePtr() {
        public void handler() {
            /* Set all the banks to the right place */
            mhavoc_ram_banksel_w.handler(0, 0);
            mhavoc_rom_banksel_w.handler(0, 0);
            bank_select = -1;
            alpha_data = 0;
            gamma_data = 0;
            alpha_rcvd = 0;
            alpha_xmtd = 0;
            gamma_rcvd = 0;
            gamma_xmtd = 0;
            player_1 = 0;
            if (gamma_timer != null) {
                timer_remove(gamma_timer);
            }
            gamma_timer = timer_pulse(TIME_IN_HZ(LS161_CLOCK / 16), 0, mhavoc_gamma_irq);
        }
    };

    /* Read from the gamma processor */
    public static ReadHandlerPtr mhavoc_gamma_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            logerror("  reading from gamma processor: %02x (%d %d)\n", gamma_data, alpha_rcvd, gamma_xmtd);
            alpha_rcvd = 1;
            gamma_xmtd = 0;
            return gamma_data;
        }
    };

    /* Read from the alpha processor */
    public static ReadHandlerPtr mhavoc_alpha_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            logerror("\t\t\t\t\treading from alpha processor: %02x (%d %d)\n", alpha_data, gamma_rcvd, alpha_xmtd);
            gamma_rcvd = 1;
            alpha_xmtd = 0;
            return alpha_data;
        }
    };

    /* Write to the gamma processor */
    public static WriteHandlerPtr mhavoc_gamma_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            logerror("  writing to gamma processor: %02x (%d %d)\n", data, gamma_rcvd, alpha_xmtd);
            gamma_rcvd = 0;
            alpha_xmtd = 1;
            alpha_data = data;
            cpu_cause_interrupt(1, M6502_INT_NMI);

            /* the sound CPU needs to reply in 250microseconds (according to Neil Bradley) */
            timer_set(TIME_IN_USEC(250), 0, null);
        }
    };

    /* Write to the alpha processor */
    public static WriteHandlerPtr mhavoc_alpha_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            logerror("\t\t\t\t\twriting to alpha processor: %02x %d %d\n", data, alpha_rcvd, gamma_xmtd);
            alpha_rcvd = 0;
            gamma_xmtd = 1;
            gamma_data = data;
        }
    };

    /* Simulates frequency and vector halt */
    public static ReadHandlerPtr mhavoc_port_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int res;

            res = readinputport(0);
            if (player_1 != 0) {
                res = (res & 0x3f) | (readinputport(5) & 0xc0);
            }

            /* Emulate the 2.4Khz source on bit 2 (divide 2.5MHz by 1024) */
            if ((cpu_gettotalcycles() & 0x400) != 0) {
                res &= ~0x02;
            } else {
                res |= 0x02;
            }

            if (avgdvg_done() != 0) {
                res |= 0x01;
            } else {
                res &= ~0x01;
            }

            if (gamma_rcvd == 1) {
                res |= 0x08;
            } else {
                res &= ~0x08;
            }

            if (gamma_xmtd == 1) {
                res |= 0x04;
            } else {
                res &= ~0x04;
            }

            return (res & 0xff);
        }
    };

    public static ReadHandlerPtr mhavoc_port_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int res;

            res = readinputport(1);

            if (alpha_rcvd == 1) {
                res |= 0x02;
            } else {
                res &= ~0x02;
            }

            if (alpha_xmtd == 1) {
                res |= 0x01;
            } else {
                res &= ~0x01;
            }

            return (res & 0xff);
        }
    };

    public static WriteHandlerPtr mhavoc_out_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0x08) == 0) {
                logerror("\t\t\t\t*** resetting gamma processor. ***\n");
                cpu_set_reset_line(1, PULSE_LINE);
                alpha_rcvd = 0;
                alpha_xmtd = 0;
                gamma_rcvd = 0;
                gamma_xmtd = 0;
            }
            player_1 = data & 0x20;
            /* Emulate the roller light (Blinks on fatal errors) */
/*TODO*///            set_led_status(2, data & 0x01);
        }
    };

    public static WriteHandlerPtr mhavoc_out_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
/*TODO*///            set_led_status(1, data & 0x01);
/*TODO*///            set_led_status(0, data & 0x02);
        }
    };
    public static TimerCallbackHandlerPtr mhavoc_gamma_irq = new TimerCallbackHandlerPtr() {
        public void handler(int param) {

            cpu_set_irq_line(1, 0, HOLD_LINE);
        }
    };

    public static WriteHandlerPtr mhavoc_irqack_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            timer_reset(gamma_timer, TIME_IN_HZ(LS161_CLOCK / 16));
            cpu_set_irq_line(1, 0, CLEAR_LINE);
        }
    };
}
