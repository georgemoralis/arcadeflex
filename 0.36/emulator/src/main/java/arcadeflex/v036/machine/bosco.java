/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */
/**
 * Changelog
 * =========
 * 21/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.machine;

//cpu imports
import static arcadeflex.v036.cpu.z80.z80H.*;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.memory.*;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;
//sndhrdw imports
import static arcadeflex.v036.sndhrdw.bosco.*;
//sound imports
import static arcadeflex.v036.sound.samples.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.bosco.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fprintf;

public class bosco {

    public static UBytePtr bosco_sharedram = new UBytePtr();
    static /*unsigned*/ char interrupt_enable_1, interrupt_enable_2, interrupt_enable_3;
    static int HiScore;
    static int Score, Score1, Score2;
    static int NextBonus, NextBonus1, NextBonus2;
    static int FirstBonus, IntervalBonus;

    static int credits;

    static Object nmi_timer_1, nmi_timer_2;

    public static InitMachineHandlerPtr bosco_init_machine = new InitMachineHandlerPtr() {
        public void handler() {
            credits = 0;
            HiScore = 20000;
            nmi_timer_1 = null;
            nmi_timer_2 = null;
            bosco_halt_w.handler(0, 0);

            memory_region(REGION_CPU1).write(0x8c00, 1);
            memory_region(REGION_CPU1).write(0x8c01, 1);
        }
    };

    public static ReadHandlerPtr bosco_sharedram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return bosco_sharedram.read(offset);
        }
    };

    public static WriteHandlerPtr bosco_sharedram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            bosco_sharedram.write(offset, data);
        }
    };

    public static ReadHandlerPtr bosco_dsw_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int bit0, bit1;

            bit0 = (input_port_0_r.handler(0) >> offset) & 1;
            bit1 = (input_port_1_r.handler(0) >> offset) & 1;

            return bit0 | (bit1 << 1);
        }
    };

    /**
     * *************************************************************************
     *
     * Emulate the custom IO chip.
     *
     **************************************************************************
     */
    static int customio_command_1;
    static /*unsigned*/ char[] customio_1 = new char[16];
    static int mode;

    public static WriteHandlerPtr bosco_customio_data_w_1 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            customio_1[offset] = (char) (data & 0xFF);

            if (errorlog != null) {
                fprintf(errorlog, "%04x: custom IO 1 offset %02x data %02x\n", cpu_get_pc(), offset, data);
            }

            switch (customio_command_1) {
                case 0x48:
                    if (offset == 1) {
                        switch (customio_1[0]) {
                            case 0x20:	 //		Mid Bang
                                sample_start(0, 0, 0);
                                break;
                            case 0x10:	 //		Big Bang
                                sample_start(1, 1, 0);
                                break;
                            case 0x50:	 //		Shot
                                sample_start(2, 2, 0);
                                break;
                        }
                    }
                    break;

                case 0x64:
                    if (offset == 0) {
                        switch (customio_1[0]) {
                            case 0x60:
                                /* 1P Score */
                                Score2 = Score;
                                Score = Score1;
                                NextBonus2 = NextBonus;
                                NextBonus = NextBonus1;
                                break;
                            case 0x68:
                                /* 2P Score */
                                Score1 = Score;
                                Score = Score2;
                                NextBonus1 = NextBonus;
                                NextBonus = NextBonus2;
                                break;
                            case 0x81:
                                Score += 10;
                                break;
                            case 0x83:
                                Score += 20;
                                break;
                            case 0x87:
                                Score += 50;
                                break;
                            case 0x88:
                                Score += 60;
                                break;
                            case 0x89:
                                Score += 70;
                                break;
                            case 0x8D:
                                Score += 200;
                                break;
                            case 0x93:
                                Score += 200;
                                break;
                            case 0x95:
                                Score += 300;
                                break;
                            case 0x96:
                                Score += 400;
                                break;
                            case 0x98:
                                Score += 600;
                                break;
                            case 0x9A:
                                Score += 800;
                                break;
                            case 0xA0:
                                Score += 500;
                                break;
                            case 0xA1:
                                Score += 1000;
                                break;
                            case 0xA2:
                                Score += 1500;
                                break;
                            case 0xA3:
                                Score += 2000;
                                break;
                            case 0xA5:
                                Score += 3000;
                                break;
                            case 0xA6:
                                Score += 4000;
                                break;
                            case 0xA7:
                                Score += 5000;
                                break;
                            case 0xA8:
                                Score += 6000;
                                break;
                            case 0xA9:
                                Score += 7000;
                                break;
                            case 0xB7:
                                Score += 100;
                                break;
                            case 0xB8:
                                Score += 120;
                                break;
                            case 0xB9:
                                Score += 140;
                                break;
                            default:
                                if (errorlog != null) {
                                    fprintf(errorlog, "unknown score: %02x\n", customio_1[0]);
                                }
                                break;
                        }
                    }
                    break;
                case 0x84:
                    if (offset == 2) {
                        int hi = (data / 16);
                        int mid = (data % 16);
                        if (customio_1[1] == 0x20) {
                            FirstBonus = (hi * 100000) + (mid * 10000);
                        }
                        if (customio_1[1] == 0x30) {
                            IntervalBonus = (hi * 100000) + (mid * 10000);
                        }
                    } else if (offset == 3) {
                        int lo = (data / 16);
                        if (customio_1[1] == 0x20) {
                            FirstBonus = FirstBonus + (lo * 1000);
                        }
                        if (customio_1[1] == 0x30) {
                            IntervalBonus = IntervalBonus + (lo * 1000);
                        }
                    }
                    break;
            }
        }
    };

    public static ReadHandlerPtr bosco_customio_data_r_1 = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (customio_command_1) {
                case 0x71:
                    if (offset == 0) {
                        int p4 = readinputport(4);

                        /* check if the user inserted a coin */
                        if ((p4 & 0x10) == 0 && credits < 99) {
                            credits++;
                        }

                        /* check if the user inserted a coin */
                        if ((p4 & 0x20) == 0 && credits < 99) {
                            credits++;
                        }

                        /* check if the user inserted a coin */
                        if ((p4 & 0x40) == 0 && credits < 99) {
                            credits++;
                        }

                        /* check for 1 player start button */
                        if ((p4 & 0x04) == 0 && credits >= 1) {
                            credits--;
                        }

                        /* check for 2 players start button */
                        if ((p4 & 0x08) == 0 && credits >= 2) {
                            credits -= 2;
                        }

                        if (mode != 0) /* switch mode */ {
                            return (p4 & 0x80);
                        } else /* credits mode: return number of credits in BCD format */ {
                            return (credits / 10) * 16 + credits % 10;
                        }
                    } else if (offset == 1) {
                        int in = readinputport(2), dir;

                        /*
					  Direction is returned as shown below:
									0
								7		1
							6				2
								5		3
									4
					  For the previous direction return 8.
                         */
                        dir = 8;
                        if ((in & 0x01) == 0) /* up */ {
                            if ((in & 0x02) == 0) /* right */ {
                                dir = 1;
                            } else if ((in & 0x08) == 0) /* left */ {
                                dir = 7;
                            } else {
                                dir = 0;
                            }
                        } else if ((in & 0x04) == 0) /* down */ {
                            if ((in & 0x02) == 0) /* right */ {
                                dir = 3;
                            } else if ((in & 0x08) == 0) /* left */ {
                                dir = 5;
                            } else {
                                dir = 4;
                            }
                        } else if ((in & 0x02) == 0) /* right */ {
                            dir = 2;
                        } else if ((in & 0x08) == 0) /* left */ {
                            dir = 6;
                        }

                        /* check fire (both impulse and hold, boscomd2 has autofire) */
                        dir |= (in & 0x30);

                        return dir;
                    }
                    break;

                case 0x94:
                    if (offset == 0) {
                        int flags = 0;
                        int lo = (Score / 1000000) % 10;
                        if (Score >= HiScore) {
                            HiScore = Score;
                            flags |= 0x80;
                        }
                        if (Score >= NextBonus) {
                            if (NextBonus == FirstBonus) {
                                NextBonus = IntervalBonus;
                                flags |= 0x40;
                            } else {
                                NextBonus += IntervalBonus;
                                flags |= 0x20;
                            }
                        }
                        return lo | flags;
                    } else if (offset == 1) {
                        int hi = (Score / 100000) % 10;
                        int lo = (Score / 10000) % 10;
                        return (hi * 16) + lo;
                    } else if (offset == 2) {
                        int hi = (Score / 1000) % 10;
                        int lo = (Score / 100) % 10;
                        return (hi * 16) + lo;
                    } else if (offset == 3) {
                        int hi = (Score / 10) % 10;
                        int lo = Score % 10;
                        return (hi * 16) + lo;
                    }
                    break;

                case 0x91:
                    if (offset <= 2) {
                        return 0;
                    }
                    break;
            }

            return -1;
        }
    };

    public static ReadHandlerPtr bosco_customio_r_1 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return customio_command_1;
        }
    };

    public static TimerCallbackHandlerPtr bosco_nmi_generate_1 = new TimerCallbackHandlerPtr() {
        public void handler(int trigger) {
            cpu_cause_interrupt(0, Z80_NMI_INT);
        }
    };

    public static WriteHandlerPtr bosco_customio_w_1 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (data != 0x10) {
                if (errorlog != null) {
                    fprintf(errorlog, "%04x: custom IO 1 command %02x\n", cpu_get_pc(), data);
                }
            }

            customio_command_1 = data;

            switch (data) {
                case 0x10:
                    if (nmi_timer_1 != null) {
                        timer_remove(nmi_timer_1);
                    }
                    nmi_timer_1 = null;
                    return;

                case 0x61:
                    mode = 1;
                    break;

                case 0xC1:
                    Score = 0;
                    Score1 = 0;
                    Score2 = 0;
                    NextBonus = FirstBonus;
                    NextBonus1 = FirstBonus;
                    NextBonus2 = FirstBonus;
                    break;

                case 0xC8:
                    break;

                case 0x84:
                    break;

                case 0x91:
                    mode = 0;
                    break;

                case 0xa1:
                    mode = 1;
                    break;
            }

            nmi_timer_1 = timer_pulse(TIME_IN_USEC(50), 0, bosco_nmi_generate_1);
        }
    };

    /**
     * *************************************************************************
     *
     * Emulate the second (!) custom IO chip.
     *
     **************************************************************************
     */
    static int customio_command_2;
    static /*unsigned*/ char[] customio_2 = new char[16];

    public static WriteHandlerPtr bosco_customio_data_w_2 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            customio_2[offset] = (char) (data & 0xFF);

            if (errorlog != null) {
                fprintf(errorlog, "%04x: custom IO 2 offset %02x data %02x\n", cpu_get_pc(), offset, data);
            }
            switch (customio_command_2) {
                case 0x82:
                    if (offset == 2) {
                        switch (customio_2[0]) {
                            case 1: // Blast Off
                                bosco_sample_play(0x0020 * 2, 0x08D7 * 2);
                                break;
                            case 2: // Alert, Alert
                                bosco_sample_play(0x8F7 * 2, 0x0906 * 2);
                                break;
                            case 3: // Battle Station
                                bosco_sample_play(0x11FD * 2, 0x07DD * 2);
                                break;
                            case 4: // Spy Ship Sighted
                                bosco_sample_play(0x19DA * 2, 0x07DE * 2);
                                break;
                            case 5: // Condition Red
                                bosco_sample_play(0x21B8 * 2, 0x079F * 2);
                                break;
                        }
                    }
                    break;
            }
        }
    };

    public static ReadHandlerPtr bosco_customio_data_r_2 = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (customio_command_2) {
                case 0x91:
                    if (offset == 2) {
                        return cpu_readmem16(0x89cc);
                    } else if (offset <= 3) {
                        return 0;
                    }
                    break;
            }

            return -1;
        }
    };

    public static ReadHandlerPtr bosco_customio_r_2 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return customio_command_2;
        }
    };

    public static TimerCallbackHandlerPtr bosco_nmi_generate_2 = new TimerCallbackHandlerPtr() {
        public void handler(int trigger) {
            cpu_cause_interrupt(1, Z80_NMI_INT);
        }
    };

    public static WriteHandlerPtr bosco_customio_w_2 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (data != 0x10) {
                if (errorlog != null) {
                    fprintf(errorlog, "%04x: custom IO 2 command %02x\n", cpu_get_pc(), data);
                }
            }

            customio_command_2 = data;

            switch (data) {
                case 0x10:
                    if (nmi_timer_2 != null) {
                        timer_remove(nmi_timer_2);
                    }
                    nmi_timer_2 = null;
                    return;
            }

            nmi_timer_2 = timer_pulse(TIME_IN_USEC(50), 0, bosco_nmi_generate_2);
        }
    };

    public static WriteHandlerPtr bosco_halt_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 1) != 0) {
                cpu_set_reset_line(1, CLEAR_LINE);
                cpu_set_reset_line(2, CLEAR_LINE);
            } else {
                cpu_set_reset_line(1, ASSERT_LINE);
                cpu_set_reset_line(2, ASSERT_LINE);
            }
        }
    };

    public static WriteHandlerPtr bosco_interrupt_enable_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            interrupt_enable_1 = (char) (data & 1);
        }
    };

    public static InterruptHandlerPtr bosco_interrupt_1 = new InterruptHandlerPtr() {
        public int handler() {
            bosco_vh_interrupt();
            /* update the background stars position */

            if (interrupt_enable_1 != 0) {
                return interrupt.handler();
            } else {
                return ignore_interrupt.handler();
            }
        }
    };

    public static WriteHandlerPtr bosco_interrupt_enable_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            interrupt_enable_2 = (char) (data & 1);
        }
    };

    public static InterruptHandlerPtr bosco_interrupt_2 = new InterruptHandlerPtr() {
        public int handler() {
            if (interrupt_enable_2 != 0) {
                return interrupt.handler();
            } else {
                return ignore_interrupt.handler();
            }
        }
    };

    public static WriteHandlerPtr bosco_interrupt_enable_3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            interrupt_enable_3 = (data & 1) == 0 ? (char) 1 : (char) 0;
        }
    };

    public static InterruptHandlerPtr bosco_interrupt_3 = new InterruptHandlerPtr() {
        public int handler() {
            if (interrupt_enable_3 != 0) {
                return nmi_interrupt.handler();
            } else {
                return ignore_interrupt.handler();
            }
        }
    };
}
