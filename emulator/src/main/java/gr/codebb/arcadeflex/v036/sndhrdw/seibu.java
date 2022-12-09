/**
 * *************************************************************************
 *
 * Seibu Sound System v1.02, designed 1986 by Seibu Kaihatsu
 *
 * The Seibu sound system comprises of a Z80A, a YM3812, a YM3931*, and an Oki
 * MSM6205. As well as sound the Z80 can controls coins and pass data to the
 * main cpu. There are a few little quirks that make it worthwhile emulating in
 * a seperate file:
 *
 * The YM3812 generates interrupt RST10, by asserting the interrupt line, and
 * placing 0xd7 on the data bus.
 *
 * The main cpu generates interrupt RST18, by asserting the interrupt line, and
 * placing 0xdf on the data bus.
 *
 * A problem can occur if both the YM3812 and the main cpu try to assert the
 * interrupt line at the same time. The effect in the old Mame emulation would
 * be for sound to stop playing - this is because a RST18 cancelled out a RST10,
 * and if a single RST10 is dropped sound stops as the YM3812 timer is not
 * reset. The problem occurs because even if both interrupts happen at the same
 * time, there can only be one value on the data bus. Obviously the real
 * hardware must have some circuit to prevent this. It is emulated by user
 * timers to control the z80 interrupt vector.
 *
 * (*What on earth _is_ the YM3931?? There are no unknown memory writes)
 *
 * Emulation by Bryan McPhail, mish@tendril.co.uk
 *
 **************************************************************************
 */

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.sndhrdw;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;

public class seibu {

    static int sound_cpu;
    public static UBytePtr seibu_shared_sound_ram = new UBytePtr();

    /**
     * ************************************************************************
     */
    public static final int VECTOR_INIT = 0;
    public static final int RST10_ASSERT = 1;
    public static final int RST10_CLEAR = 2;
    public static final int RST18_ASSERT = 3;
    public static final int RST18_CLEAR = 4;

    static int irq1, irq2;
    public static TimerCallbackHandlerPtr setvector_callback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {

            switch (param) {
                case VECTOR_INIT:
                    irq1 = irq2 = 0xff;
                    break;

                case RST10_ASSERT:
                    irq1 = 0xd7;
                    break;

                case RST10_CLEAR:
                    irq1 = 0xff;
                    break;

                case RST18_ASSERT:
                    irq2 = 0xdf;
                    break;

                case RST18_CLEAR:
                    irq2 = 0xff;
                    break;
            }

            cpu_irq_line_vector_w(sound_cpu, 0, irq1 & irq2);
            if ((irq1 & irq2) == 0xff) /* no IRQs pending */ {
                cpu_set_irq_line(sound_cpu, 0, CLEAR_LINE);
            } else /* IRQ pending */ {
                cpu_set_irq_line(sound_cpu, 0, ASSERT_LINE);
            }
        }
    };

    public static WriteHandlerPtr seibu_rst10_ack = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* Unused for now */
        }
    };

    public static WriteHandlerPtr seibu_rst18_ack = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            timer_set(TIME_NOW, RST18_CLEAR, setvector_callback);
        }
    };
    public static WriteYmHandlerPtr seibu_ym3812_irqhandler = new WriteYmHandlerPtr() {
        public void handler(int linestate) {
            if (linestate != 0) {
                timer_set(TIME_NOW, RST10_ASSERT, setvector_callback);
            } else {
                timer_set(TIME_NOW, RST10_CLEAR, setvector_callback);
            }
        }
    };

    /**
     * ************************************************************************
     */
    /* Use this if the sound cpu is cpu 1 */
    public static InitMachineHandlerPtr seibu_sound_init_1 = new InitMachineHandlerPtr() {
        public void handler() {
            sound_cpu = 1;
            setvector_callback.handler(VECTOR_INIT);
        }
    };
    public static InitMachineHandlerPtr seibu_sound_init_2 = new InitMachineHandlerPtr() {
        public void handler() {
            /* Use this if the sound cpu is cpu 2 */
            sound_cpu = 2;
            setvector_callback.handler(VECTOR_INIT);
        }
    };

    /**
     * ************************************************************************
     */
    public static WriteHandlerPtr seibu_bank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM;

            if (sound_cpu == 1) {
                RAM = memory_region(REGION_CPU2);
            } else {
                RAM = memory_region(REGION_CPU3);
            }

            if ((data & 1) != 0) {
                cpu_setbank(1, new UBytePtr(RAM, 0x0000));
            } else {
                cpu_setbank(1, new UBytePtr(RAM, 0x10000));
            }
        }
    };

    public static ReadHandlerPtr seibu_soundlatch_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return seibu_shared_sound_ram.read(offset << 1);
        }
    };

    public static WriteHandlerPtr seibu_soundclear_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            seibu_shared_sound_ram.write(0, data);
        }
    };

    public static WriteHandlerPtr seibu_soundlatch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            seibu_shared_sound_ram.write(offset, data);
            if (offset == 0xc && seibu_shared_sound_ram.read(0) != 0) {
                timer_set(TIME_NOW, RST18_ASSERT, setvector_callback);
            }
        }
    };

    public static WriteHandlerPtr seibu_main_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            seibu_shared_sound_ram.write(offset << 1, data);
        }
    };

    /**
     * ************************************************************************
     */
    public static ReadHandlerPtr sound_cpu_spin = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr RAM;

            if (sound_cpu == 1) {
                RAM = memory_region(REGION_CPU2);
            } else {
                RAM = memory_region(REGION_CPU3);
            }

            if (cpu_get_pc() == 0x129 && RAM.read(0x201c) == 0) {
                cpu_spinuntil_int();
            }

            return RAM.read(0x201c + offset);
        }
    };

    public static void install_seibu_sound_speedup(int cpu) {
        install_mem_read_handler(cpu, 0x201c, 0x201d, sound_cpu_spin);
    }

    /**
     * ************************************************************************
     */
    /* NOTICE!  This is not currently correct, this table works for the first
     128 bytes, but goes wrong after that.  I suspect the bytes in this table
     are shifted according to an address line.  I have not confirmed the pattern
     repeats after 128 bytes, it may be more...
	
     There is also a 0xff fill at the end of the rom.
	
     */
    /* Game using encrypted sound cpu - Raiden, Dynamite Duke, Dead Angle */
    public static void seibu_sound_decrypt() {
        UBytePtr RAM = memory_region(REGION_CPU3);
        int xor_table[] = {
            0x00, 0x00, 0x10, 0x10, 0x08, 0x00, 0x00, 0x18,
            0x00, 0x00, 0x10, 0x10, 0x08, 0x08, 0x18, 0x18,
            0x00, 0x00, 0x00, 0x10, 0x08, 0x08, 0x18, 0x18,
            0x00, 0x00, 0x00, 0x10, 0x08, 0x08, 0x18, 0x18,
            0x00, 0x00, 0x10, 0x10, 0x08, 0x08, 0x18, 0x18,
            0x00, 0x00, 0x10, 0x10, 0x08, 0x08, 0x18, 0x18,
            0x00, 0x00, 0x10, 0x10, 0x08, 0x08, 0x18, 0x18,
            0x00, 0x00, 0x10, 0x10, 0x08, 0x08, 0x18, 0x18,
            0x00, 0x00, 0x00, 0x00, 0x08, 0x08, 0x08, 0x08,
            0x00, 0x00, 0x00, 0x00, 0x08, 0x08, 0x08, 0x08,
            0x00, 0x00, 0x00, 0x00, 0x08, 0x08, 0x08, 0x08,
            0x00, 0x00, 0x00, 0x00, 0x08, 0x08, 0x08, 0x08,
            0x00, 0x00, 0x00, 0x00, 0x08, 0x08, 0x08, 0x08,
            0x00, 0x00, 0x00, 0x00, 0x08, 0x08, 0x08, 0x08,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x08,
            0x00, 0x00, 0x00, 0x00, 0x08, 0x08, 0x08, 0x08,};
        int i;

        for (i = 0; i < 0x18000; i++) {
            RAM.write(i, RAM.read(i) ^ xor_table[i % 128]);
        }
    }
}
