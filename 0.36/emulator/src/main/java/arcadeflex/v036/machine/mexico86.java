/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
/**
 * Changelog
 * =========
 * 03/02/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.machine;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.mame.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;

public class mexico86 {

    public static UBytePtr mexico86_protection_ram = new UBytePtr();

    /**
     * *************************************************************************
     *
     * Mexico 86 68705 protection interface
     *
     * The following is ENTIRELY GUESSWORK!!!
     *
     **************************************************************************
     */
    public static InterruptHandlerPtr mexico86_m68705_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            /* I don't know how to handle the interrupt line so I just toggle it every time. */
            if ((cpu_getiloops() & 1) != 0) {
                cpu_set_irq_line(2, 0, CLEAR_LINE);
            } else {
                cpu_set_irq_line(2, 0, ASSERT_LINE);
            }

            return 0;
        }
    };

    static /*unsigned*/ char portA_in, portA_out, ddrA;

    public static ReadHandlerPtr mexico86_68705_portA_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            //if(errorlog!=null) fprintf(errorlog,"%04x: 68705 port A read %02x\n",cpu_get_pc(),portA_in);
            return (portA_out & ddrA) | (portA_in & ~ddrA);
        }
    };

    public static WriteHandlerPtr mexico86_68705_portA_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            //if(errorlog!=null) fprintf(errorlog,"%04x: 68705 port A write %02x\n",cpu_get_pc(),data);
            portA_out = (char) (data & 0xFF);
        }
    };

    public static WriteHandlerPtr mexico86_68705_ddrA_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            ddrA = (char) (data & 0xFF);
        }
    };

    /*
	 *  Port B connections:
	 *
	 *  all bits are logical 1 when read (+5V pullup)
	 *
	 *  0   W  enables latch which holds data from main Z80 memory
	 *  1   W  loads the latch which holds the low 8 bits of the address of
	 *               the main Z80 memory location to access
	 *  2   W  0 = read input ports, 1 = access Z80 memory
	 *  3   W  clocks main Z80 memory access
	 *  4   W  selects Z80 memory access direction (0 = write 1 = read)
	 *  5   W  clocks a flip-flop which causes IRQ on the main Z80
	 *  6   W  not used?
	 *  7   W  not used?
     */
    static /*unsigned*/ char portB_in, portB_out, ddrB;

    public static ReadHandlerPtr mexico86_68705_portB_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (portB_out & ddrB) | (portB_in & ~ddrB);
        }
    };

    static int address, latch;

    public static WriteHandlerPtr mexico86_68705_portB_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            //if(errorlog!=null) fprintf(errorlog,"%04x: 68705 port B write %02x\n",cpu_get_pc(),data);

            if ((ddrB & 0x01) != 0 && (~data & 0x01) != 0 && (portB_out & 0x01) != 0) {
                portA_in = (char) (latch & 0xFF);
            }
            if ((ddrB & 0x02) != 0 && (data & 0x02) != 0 && (~portB_out & 0x02) != 0) /* positive edge trigger */ {
                address = portA_out;
                //if (address >= 0x80) if(errorlog!=null) fprintf(errorlog,"%04x: 68705 address %02x\n",cpu_get_pc(),portA_out);
            }
            if ((ddrB & 0x08) != 0 && (~data & 0x08) != 0 && (portB_out & 0x08) != 0) {
                if ((data & 0x10) != 0) /* read */ {
                    if ((data & 0x04) != 0) {
                        //if(errorlog!=null) fprintf(errorlog,"%04x: 68705 read %02x from address %04x\n",cpu_get_pc(),shared[0x800+address],address);
                        latch = mexico86_protection_ram.read(address);
                    } else {
                        //if(errorlog!=null) fprintf(errorlog,"%04x: 68705 read input port %04x\n",cpu_get_pc(),address);
                        latch = readinputport((address & 1) + 1);
                    }
                } else /* write */ {
                    //if(errorlog!=null) fprintf(errorlog,"%04x: 68705 write %02x to address %04x\n",cpu_get_pc(),portA_out,address);
                    mexico86_protection_ram.write(address, portA_out);
                }
            }
            if ((ddrB & 0x20) != 0 && (data & 0x20) != 0 && (~portB_out & 0x20) != 0) {
                cpu_irq_line_vector_w(0, 0, mexico86_protection_ram.read(0));
                //		cpu_set_irq_line(0,0,HOLD_LINE);
                cpu_set_irq_line(0, 0, PULSE_LINE);
            }
            if ((ddrB & 0x40) != 0 && (~data & 0x40) != 0 && (portB_out & 0x40) != 0) {
                if (errorlog != null) {
                    fprintf(errorlog, "%04x: 68705 unknown port B bit %02x\n", cpu_get_pc(), data);
                }
            }
            if ((ddrB & 0x80) != 0 && (~data & 0x80) != 0 && (portB_out & 0x80) != 0) {
                if (errorlog != null) {
                    fprintf(errorlog, "%04x: 68705 unknown port B bit %02x\n", cpu_get_pc(), data);
                }
            }

            portB_out = (char) (data & 0xFF);
        }
    };

    public static WriteHandlerPtr mexico86_68705_ddrB_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            ddrB = (char) (data & 0xFF);
        }
    };
}
