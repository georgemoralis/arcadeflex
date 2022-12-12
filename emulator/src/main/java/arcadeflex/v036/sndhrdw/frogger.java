/**
 * ported to 0.36
 */
package arcadeflex.v036.sndhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrf.*;

public class frogger {

    /* The timer clock which feeds the upper 4 bits of    					*/
 /* AY-3-8910 port A is based on the same clock        					*/
 /* feeding the sound CPU Z80.  It is a divide by      					*/
 /* 5120, formed by a standard divide by 512,        					*/
 /* followed by a divide by 10 using a 4 bit           					*/
 /* bi-quinary count sequence. (See LS90 data sheet    					*/
 /* for an example).                                   					*/
 /*																		*/
 /* Bit 4 comes from the output of the divide by 1024  					*/
 /*       0, 1, 0, 1, 0, 1, 0, 1, 0, 1									*/
 /* Bit 3 comes from the QC output of the LS90 producing a sequence of	*/
 /* 		 0, 0, 1, 1, 0, 0, 1, 1, 1, 0									*/
 /* Bit 6 comes from the QD output of the LS90 producing a sequence of	*/
 /*		 0, 0, 0, 0, 1, 0, 0, 0, 0, 1									*/
 /* Bit 7 comes from the QA output of the LS90 producing a sequence of	*/
 /*		 0, 0, 0, 0, 0, 1, 1, 1, 1, 1			 						*/
    static int frogger_timer[]
            = {
                0x00, 0x10, 0x08, 0x18, 0x40, 0x90, 0x88, 0x98, 0x88, 0xd0
            };

    /* need to protect from totalcycles overflow */
    static int last_totalcycles_frogger = 0;
    static int clock_frogger;
    public static ReadHandlerPtr frogger_portB_r = new ReadHandlerPtr() {
        public int handler(int offset) {

            /* number of Z80 clock cycles to count */
            int current_totalcycles;

            current_totalcycles = cpu_gettotalcycles();
            clock_frogger = (clock_frogger + (current_totalcycles - last_totalcycles_frogger)) % 5120;

            last_totalcycles_frogger = current_totalcycles;

            return frogger_timer[clock_frogger / 512];
        }
    };

    static int last_frogger;
    public static WriteHandlerPtr frogger_sh_irqtrigger_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if (last_frogger == 0 && (data & 0x08) != 0) {
                /* setting bit 3 low then high triggers IRQ on the sound CPU */
                cpu_cause_interrupt(1, 0xff);
            }

            last_frogger = data & 0x08;
        }
    };
    static int last2_frogger;
    public static WriteHandlerPtr frogger2_sh_irqtrigger_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if (last2_frogger == 0 && (data & 0x01) != 0) {
                /* setting bit 0 low then high triggers IRQ on the sound CPU */
                cpu_cause_interrupt(1, 0xff);
            }
            last2_frogger = data & 0x01;
        }
    };
}
