/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
/**
 * Changelog
 * =========
 * 16/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.machine;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class jrpacman {

    static int speedcheat = 0;
    /* a well known hack allows to make JrPac Man run at four times */
 /* his usual speed. When we start the emulation, we check if the */
 /* hack can be applied, and set this flag accordingly. */


    public static InitMachineHandlerPtr jrpacman_init_machine = new InitMachineHandlerPtr() {
        public void handler() {
            UBytePtr RAM = memory_region(REGION_CPU1);

            /* check if the loaded set of ROMs allows the Pac Man speed hack */
            if (RAM.read(0x180b) == 0xbe || RAM.read(0x180b) == 0x01) {
                speedcheat = 1;
            } else {
                speedcheat = 0;
            }
        }
    };

    public static InterruptHandlerPtr jrpacman_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            UBytePtr RAM = memory_region(REGION_CPU1);

            /* speed up cheat */
            if (speedcheat != 0) {
                if ((readinputport(3) & 1) != 0) /* check status of the fake dip switch */ {
                    /* activate the cheat */
                    RAM.write(0x180b, 0x01);
                } else {
                    /* remove the cheat */
                    RAM.write(0x180b, 0xbe);
                }
            }

            return interrupt.handler();
        }
    };
}
