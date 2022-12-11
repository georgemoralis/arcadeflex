/*
 * ported to v0.36
 *
 */
package arcadeflex.v036.machine;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class pacman {

    public static int speedcheat = 0;
    /* a well known hack allows to make Pac Man run at four times */
 /* his usual speed. When we start the emulation, we check if the */
 /* hack can be applied, and set this flag accordingly. */

    public static InitMachineHandlerPtr pacman_init_machine = new InitMachineHandlerPtr() {
        public void handler() {
            UBytePtr RAM = memory_region(REGION_CPU1);

            /* check if the loaded set of ROMs allows the Pac Man speed hack */
            if ((RAM.read(0x180b) == 0xbe && RAM.read(0x1ffd) == 0x00)
                    || (RAM.read(0x180b) == 0x01 && RAM.read(0x1ffd) == 0xbd)) {
                speedcheat = 1;
            } else {
                speedcheat = 0;
            }
        }
    };

    public static InterruptHandlerPtr pacman_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            UBytePtr RAM = memory_region(REGION_CPU1);

            /* speed up cheat */
            if (speedcheat != 0) {
                if ((readinputport(4) & 1) != 0) /* check status of the fake dip switch */ {
                    /* activate the cheat */
                    RAM.write(0x180b, 0x01);
                    RAM.write(0x1ffd, 0xbd);
                } else {
                    /* remove the cheat */
                    RAM.write(0x180b, 0xbe);
                    RAM.write(0x1ffd, 0x00);
                }
            }

            return interrupt.handler();
        }
    };
}
