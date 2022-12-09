/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.machine;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;

import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static arcadeflex.v036.mame.inptport.*;

public class mappy {

    public static UBytePtr mappy_sharedram = new UBytePtr();
    public static UBytePtr mappy_customio_1 = new UBytePtr();
    public static UBytePtr mappy_customio_2 = new UBytePtr();

    static char interrupt_enable_1, interrupt_enable_2;
    static int credits, coin, start1, start2;
    static int io_chip_1_enabled, io_chip_2_enabled;

    public static InitMachineHandlerPtr mappy_init_machine = new InitMachineHandlerPtr() {
        public void handler() {
            /* Reset all flags */
            credits = coin = start1 = start2 = 0;
            interrupt_enable_1 = interrupt_enable_2 = 0;
        }
    };

    public static InitMachineHandlerPtr motos_init_machine = new InitMachineHandlerPtr() {
        public void handler() {
            /* Reset all flags */
            credits = coin = start1 = start2 = 0;
        }
    };

    public static ReadHandlerPtr mappy_sharedram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return mappy_sharedram.read(offset);
        }
    };

    public static ReadHandlerPtr mappy_sharedram_r2 = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* to speed up emulation, we check for the loop the sound CPU sits in most of the time
		   and end the current iteration (things will start going again with the next IRQ) */
            if (offset == 0x010a - 0x40 && mappy_sharedram.read(offset) == 0) {
                cpu_spinuntil_int();
            }
            return mappy_sharedram.read(offset);
        }
    };

    public static ReadHandlerPtr digdug2_sharedram_r2 = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* to speed up emulation, we check for the loop the sound CPU sits in most of the time
		   and end the current iteration (things will start going again with the next IRQ) */
            if (offset == 0x0a1 - 0x40 && mappy_sharedram.read(offset) == 0 && cpu_get_pc() == 0xe383) {
                cpu_spinuntil_int();
            }
            return mappy_sharedram.read(offset);
        }
    };

    public static ReadHandlerPtr motos_sharedram_r2 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return mappy_sharedram.read(offset);
        }
    };

    public static ReadHandlerPtr todruaga_sharedram_r2 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return mappy_sharedram.read(offset);
        }
    };

    public static WriteHandlerPtr mappy_sharedram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            mappy_sharedram.write(offset, data);
        }
    };

    public static WriteHandlerPtr mappy_customio_w_1 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            mappy_customio_1.write(offset, data);
        }
    };

    public static WriteHandlerPtr mappy_customio_w_2 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            mappy_customio_2.write(offset, data);
        }
    };

    public static WriteHandlerPtr mappy_reset_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            io_chip_1_enabled = io_chip_2_enabled = 0;
            cpu_set_reset_line(1, PULSE_LINE);
        }
    };

    public static WriteHandlerPtr mappy_io_chips_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            io_chip_1_enabled = io_chip_2_enabled = 1;
        }
    };

    /**
     * ***********************************************************************************
     *
     * Mappy custom I/O ports
     *
     ************************************************************************************
     */
    static int crednum_1[] = {1, 2, 3, 6, 1, 3, 1, 2};
    static int credden_1[] = {1, 1, 1, 1, 2, 2, 3, 3};
    static int lastval_1;
    static int lastval_2;
    static int testvals1[] = {8, 4, 6, 14, 13, 9, 13};
    public static ReadHandlerPtr mappy_customio_r_1 = new ReadHandlerPtr() {
        public int handler(int offset) {

            int val, temp, mode = mappy_customio_1.read(8);

            if (errorlog != null) {
                fprintf(errorlog, "I/O read 1: mode %d offset %d\n", mode, offset);
            }

            /* mode 3 is the standard, and returns actual important values */
            if (mode == 1 || mode == 3) {
                switch (offset) {
                    case 0: /* Coin slots, low nibble of port 4 */ {

                        val = readinputport(4) & 0x0f;

                        /* bit 0 is a trigger for the coin slot */
                        if ((val & 1) != 0 && ((val ^ lastval_1) & 1) != 0) {
                            ++credits;
                        }

                        return lastval_1 = val;
                    }

                    case 1: /* Start buttons, high nibble of port 4 */ {

                        temp = readinputport(1) & 7;
                        val = readinputport(4) >> 4;

                        /* bit 0 is a trigger for the 1 player start */
                        if ((val & 1) != 0 && ((val ^ lastval_2) & 1) != 0) {
                            if (credits >= credden_1[temp]) {
                                credits -= credden_1[temp];
                            } else {
                                val &= ~1;	/* otherwise you can start with no credits! */
                            }
                        }
                        /* bit 1 is a trigger for the 2 player start */
                        if ((val & 2) != 0 && ((val ^ lastval_2) & 2) != 0) {
                            if (credits >= 2 * credden_1[temp]) {
                                credits -= 2 * credden_1[temp];
                            } else {
                                val &= ~2;	/* otherwise you can start with no credits! */
                            }
                        }

                        return lastval_2 = val;
                    }

                    case 2:
                        /* High BCD of credits */
                        temp = readinputport(1) & 7;
                        return (credits * crednum_1[temp] / credden_1[temp]) / 10;

                    case 3:
                        /* Low BCD of credits */
                        temp = readinputport(1) & 7;
                        return (credits * crednum_1[temp] / credden_1[temp]) % 10;

                    case 4:
                        /* Player 1 joystick */
                        return readinputport(3) & 0x0f;

                    case 5:
                        /* Player 1 buttons */
                        return readinputport(3) >> 4;

                    case 6:
                        /* Player 2 joystick */
                        return readinputport(5) & 0x0f;

                    case 7:
                        /* Player 2 joystick */
                        return readinputport(5) >> 4;
                }
            } /* mode 5 values are actually checked against these numbers during power up */ else if (mode == 5) {

                if (offset >= 1 && offset <= 7) {
                    return testvals1[offset - 1];
                }
            }

            /* by default, return what was stored there */
            return mappy_customio_1.read(offset);
        }
    };

    static int testvals2[] = {8, 4, 6, 14, 13, 9, 13};
    public static ReadHandlerPtr mappy_customio_r_2 = new ReadHandlerPtr() {
        public int handler(int offset) {
            int mode = mappy_customio_2.read(8);

            if (errorlog != null) {
                fprintf(errorlog, "I/O read 2: mode %d, offset %d\n", mappy_customio_2.read(8), offset);
            }

            /* mode 4 is the standard, and returns actual important values */
            if (mode == 4) {
                switch (offset) {
                    case 0:
                        /* DSW1, low nibble */
                        return readinputport(1) & 0x0f;

                    case 1:
                        /* DSW1, high nibble */
                        return readinputport(1) >> 4;

                    case 2:
                        /* DSW0, low nibble */
                        return readinputport(0) & 0x0f;

                    case 4:
                        /* DSW0, high nibble */
                        return readinputport(0) >> 4;

                    case 6:
                        /* DSW2 - service switch */
                        return readinputport(2) & 0x0f;

                    case 3:
                    /* read, but unknown */
                    case 5:
                    /* read, but unknown */
                    case 7:
                        /* read, but unknown */
                        return 0;
                }
            } /* mode 5 values are actually checked against these numbers during power up */ else if (mode == 5) {

                if (offset >= 1 && offset <= 7) {
                    return testvals2[offset - 1];
                }
            }

            /* by default, return what was stored there */
            return mappy_customio_2.read(offset);
        }
    };

    /**
     * ***********************************************************************************
     *
     * Dig Dug 2 custom I/O ports
     *
     ************************************************************************************
     */
    static int crednum_2[] = {1, 1, 2, 2};
    static int credden_2[] = {1, 2, 1, 3};
    static int lastval_3;
    static int lastval_4;
    public static ReadHandlerPtr digdug2_customio_r_1 = new ReadHandlerPtr() {
        public int handler(int offset) {

            int val, temp, mode = mappy_customio_1.read(8);

            /*if (errorlog != 0)
			fprintf (errorlog, "I/O read 1: mode %d offset %d\n", mode, offset);*/
            if (io_chip_1_enabled != 0) {
                /* mode 3 is the standard, and returns actual important values */
                if (mode == 1 || mode == 3) {
                    switch (offset) {
                        case 0: /* Coin slots, low nibble of port 4 */ {

                            val = readinputport(4) & 0x0f;

                            /* bit 0 is a trigger for the coin slot */
                            if ((val & 1) != 0 && ((val ^ lastval_3) & 1) != 0) {
                                ++credits;
                            }

                            return lastval_3 = val;
                        }

                        case 1: /* Start buttons, high nibble of port 4 */ {

                            temp = (readinputport(0) >> 6) & 3;
                            val = readinputport(4) >> 4;

                            /* bit 0 is a trigger for the 1 player start */
                            if ((val & 1) != 0 && ((val ^ lastval_4) & 1) != 0) {
                                if (credits >= credden_2[temp]) {
                                    credits -= credden_2[temp];
                                }
                            }
                            /* bit 1 is a trigger for the 2 player start */
                            if ((val & 2) != 0 && ((val ^ lastval_4) & 2) != 0) {
                                if (credits >= 2 * credden_2[temp]) {
                                    credits -= 2 * credden_2[temp];
                                }
                            }

                            return lastval_4 = val;
                        }

                        case 2:
                            /* High BCD of credits */
                            temp = (readinputport(0) >> 6) & 3;
                            return (credits * crednum_2[temp] / credden_2[temp]) / 10;

                        case 3:
                            /* Low BCD of credits */
                            temp = (readinputport(0) >> 6) & 3;
                            return (credits * crednum_2[temp] / credden_2[temp]) % 10;

                        case 4:
                            /* Player 1 joystick */
                            return readinputport(3) & 0x0f;

                        case 5:
                            /* Player 1 buttons */
                            return readinputport(3) >> 4;

                        case 6:
                            /* Player 2 joystick */
                            return readinputport(5) & 0x0f;

                        case 7:
                            /* Player 2 buttons */
                            return readinputport(5) >> 4;
                    }
                }
            }
            /* by default, return what was stored there */
            return mappy_customio_1.read(offset);
        }
    };

    public static ReadHandlerPtr digdug2_customio_r_2 = new ReadHandlerPtr() {
        public int handler(int offset) {
            int mode = mappy_customio_2.read(8);

            /*if (errorlog != 0)
			fprintf (errorlog, "I/O read 2: mode %d, offset %d\n", mode, offset);*/
            if (io_chip_2_enabled != 0) {
                /* mode 4 is the standard, and returns actual important values */
                if (mode == 4) {
                    switch (offset) {
                        case 2:
                            /* DSW0, low nibble */
                            return readinputport(0) & 0x0f;

                        case 4:
                            /* DSW0, high nibble */
                            return readinputport(0) >> 4;

                        case 5:
                            /* DSW1, high nibble */
                            return readinputport(1) >> 4;

                        case 6:
                            /* DSW1, low nibble */
                            return readinputport(1) & 0x0f;

                        case 7:
                            /* DSW2 - service switch */
                            return readinputport(2) & 0x0f;

                        case 0:
                        /* read, but unknown */
                        case 1:
                        /* read, but unknown */
                        case 3:
                            /* read, but unknown */
                            return 0;
                    }
                }
            }
            /* by default, return what was stored there */
            return mappy_customio_2.read(offset);
        }
    };

    /**
     * ***********************************************************************************
     *
     * Motos custom I/O ports
     *
     ************************************************************************************
     */
    static int lastval_5;
    public static ReadHandlerPtr motos_customio_r_1 = new ReadHandlerPtr() {
        public int handler(int offset) {
            int val, mode = mappy_customio_1.read(8);

            if (errorlog != null) {
                fprintf(errorlog, "I/O read 1: mode %d offset %d\n", mode, offset);
            }

            /* mode 1 is the standard, and returns actual important values */
            if (mode == 1) {
                switch (offset) {
                    case 0: /* Coin slots, low nibble of port 3 */ {

                        val = readinputport(3) & 0x0f;

                        /* bit 0 is a trigger for the coin slot */
                        if ((val & 1) != 0 && ((val ^ lastval_5) & 1) != 0) {
                            ++credits;
                        }

                        return lastval_5 = val;
                    }

                    case 1:
                        /* Player 1 joystick */
                        return readinputport(2) & 0x0f;
                    case 3:
                        /* Start buttons, high nibble of port 3 */
                        return readinputport(3) >> 4;
                    case 9:
                        return 0;
                    case 2:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        /* Player 2 joystick */
                        return readinputport(4) & 0x0f;
                }
            } else if (mode == 8) /* I/O tests chip 1 */ {
                switch (offset) {
                    case 0:
                        return 0x06;
                    //break;
                    case 1:
                        return 0x09;
                    //break;
                    default:
                        /* by default, return what was stored there */
                        return mappy_customio_2.read(offset);
                }
            }

            /* by default, return what was stored there */
            return mappy_customio_1.read(offset);
        }
    };

    public static ReadHandlerPtr motos_customio_r_2 = new ReadHandlerPtr() {
        public int handler(int offset) {
            int mode = mappy_customio_2.read(8);

            if (errorlog != null) {
                fprintf(errorlog, "I/O read 2: mode %d, offset %d\n", mode, offset);
            }

            /* mode 9 is the standard, and returns actual important values */
            if (mode == 9) {
                switch (offset) {
                    case 2:
                        /* DSW0, low nibble */
                        return readinputport(0) & 0x0f;

                    case 4:
                        /* DSW0, high nibble */
                        return readinputport(0) >> 4;

                    case 6:
                        /* DSW1, high nibble + Player 1 buttons, high nibble + Player 2? button, high nibble */

                        return (readinputport(1) >> 4) | (readinputport(2) >> 4) | (readinputport(4) >> 4);
                    case 0:
                    case 1:
                    case 3:
                    case 5:
                    case 7:
                        return 0;
                }
            } else if (mode == 8) /* I/O tests chip 2 */ {
                switch (offset) {
                    case 0:
                        return 0x06;
                    //break;
                    case 1:
                        return 0x09;
                    //break;
                    default:
                        /* by default, return what was stored there */
                        return mappy_customio_2.read(offset);
                }
            }

            /* by default, return what was stored there */
            return mappy_customio_2.read(offset);
        }
    };

    /**
     * ***********************************************************************************
     *
     * Tower of Druaga custom I/O ports
     *
     ************************************************************************************
     */
    static int crednum_3[] = {1, 1, 2, 2};
    static int credden_3[] = {1, 2, 1, 3};
    static int lastval_6;
    static int lastval_7;
    public static ReadHandlerPtr todruaga_customio_r_1 = new ReadHandlerPtr() {
        public int handler(int offset) {

            int val, temp, mode = mappy_customio_1.read(8);

            if (errorlog != null) {
                fprintf(errorlog, "%04x: I/O read 1: mode %d offset %d\n", cpu_get_pc(), mode, offset);
            }

            if (io_chip_1_enabled != 0) {
                /* mode 3 is the standard, and returns actual important values */
                if (mode == 1 || mode == 3) {
                    switch (offset) {
                        case 0: /* Coin slots, low nibble of port 5 */ {

                            val = readinputport(5) & 0x0f;

                            /* bit 0 is a trigger for the coin slot */
                            if ((val & 1) != 0 && ((val ^ lastval_6) & 1) != 0) {
                                ++credits;
                            }

                            return lastval_6 = val;
                        }

                        case 1: /* Start buttons, high nibble of port 5 */ {

                            temp = (readinputport(0) >> 6) & 3;
                            val = readinputport(5) >> 4;
                            val |= (readinputport(3) & 0x80) >> 7;
                            /* player 1 start */

 /* bit 0 is a trigger for the 1 player start */
                            if ((val & 1) != 0 && ((val ^ lastval_7) & 1) != 0) {
                                if (credits >= credden_3[temp]) {
                                    credits -= credden_3[temp];
                                }
                            }
                            /* bit 1 is a trigger for the 2 player start */
                            if ((val & 2) != 0 && ((val ^ lastval_7) & 2) != 0) {
                                if (credits >= 2 * credden_3[temp]) {
                                    credits -= 2 * credden_3[temp];
                                }
                            }

                            return lastval_7 = val;
                        }

                        case 2:
                            /* High BCD of credits */
                            temp = (readinputport(0) >> 6) & 3;
                            return (credits * crednum_3[temp] / credden_3[temp]) / 10;

                        case 3:
                            /* Low BCD of credits */
                            temp = (readinputport(0) >> 6) & 3;
                            return (credits * crednum_3[temp] / credden_3[temp]) % 10;

                        case 4:
                            /* Player 1 joystick */
                            return readinputport(3) & 0x0f;

                        case 5:
                            /* Player 1 buttons */
                            return readinputport(3) >> 4;

                        case 6:
                            /* Player 2 joystick */
                            return readinputport(6) & 0x0f;

                        case 7:
                            /* Player 2 buttons */
                            return readinputport(6) >> 4;
                    }
                }
            }
            /* by default, return what was stored there */
            return mappy_customio_1.read(offset);
        }
    };

    public static ReadHandlerPtr todruaga_customio_r_2 = new ReadHandlerPtr() {
        public int handler(int offset) {
            int mode = mappy_customio_2.read(8);

            if (errorlog != null) {
                fprintf(errorlog, "%04x: I/O read 2: mode %d, offset %d\n", cpu_get_pc(), mode, offset);
            }

            if (io_chip_1_enabled != 0) {
                /* mode 4 is the standard, and returns actual important values */
                if (mode == 4) {
                    switch (offset) {
                        case 2:
                            /* DSW0, low nibble */
                            return readinputport(0) & 0x0f;

                        case 4:
                            /* DSW0, high nibble */
                            return readinputport(0) >> 4;

                        case 5:
                            /* DSW1, high nibble */
                            return readinputport(1) >> 4;

                        case 6:
                            /* DSW1, low nibble */
                            return readinputport(1) & 0x0f;

                        case 7:
                            /* DSW2 - service switch */
                            return readinputport(2) & 0x0f;

                        case 0:
                        /* read, but unknown */
                        case 1:
                        /* read, but unknown */
                        case 3:
                            /* read, but unknown */
                            return 0;
                    }
                }
            }

            /* by default, return what was stored there */
            return mappy_customio_2.read(offset);
        }
    };

    public static WriteHandlerPtr mappy_interrupt_enable_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            interrupt_enable_1 = (char) offset;
        }
    };

    public static InterruptHandlerPtr mappy_interrupt_1 = new InterruptHandlerPtr() {
        public int handler() {
            if (interrupt_enable_1 != 0) {
                return interrupt.handler();
            } else {
                return ignore_interrupt.handler();
            }
        }
    };

    public static WriteHandlerPtr mappy_interrupt_enable_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            interrupt_enable_2 = (char) offset;
        }
    };

    public static InterruptHandlerPtr mappy_interrupt_2 = new InterruptHandlerPtr() {
        public int handler() {
            if (interrupt_enable_2 != 0) {
                return interrupt.handler();
            } else {
                return ignore_interrupt.handler();
            }
        }
    };

    public static WriteHandlerPtr mappy_cpu_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_set_halt_line(1, offset != 0 ? CLEAR_LINE : ASSERT_LINE);
        }
    };
}
