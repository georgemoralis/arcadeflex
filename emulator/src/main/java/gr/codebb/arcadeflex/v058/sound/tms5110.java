/*
 * ported to 0.58
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v058.sound;

import gr.codebb.arcadeflex.common.PtrLib.ShortPtr;
import static gr.codebb.arcadeflex.common.libc.cstdlib.rand;
import static gr.codebb.arcadeflex.common.libc.cstring.memset;
import static gr.codebb.arcadeflex.v036.platform.libc_old.sizeof;
import static gr.codebb.arcadeflex.v037b7.sound.tms5110H.*;
import static gr.codebb.arcadeflex.v037b7.sound.tms5110r.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.*;


public class tms5110 {

    /* Pull in the ROM tables */
 /* these contain data that describes the 64 bits FIFO */
    public static final int FIFO_SIZE = 64;
    static char[]/*UINT8*/ u8_fifo = new char[FIFO_SIZE];
    static char /*UINT8*/ u8_fifo_head;
    static char /*UINT8*/ u8_fifo_tail;
    static char /*UINT8*/ u8_fifo_count;

    /* these contain global status bits */
    static char /*UINT8*/ u8_PDC;
    static char /*UINT8*/ u8_CTL_pins;
    static char /*UINT8*/ u8_speaking_now;
    static char /*UINT8*/ u8_speak_delay_frames;
    static char /*UINT8*/ u8_talk_status;

    static M0_callbackPtr M0_callback;

    /* these contain data describing the current and previous voice frames */
    static char old_energy;
    static char old_pitch;
    static int[] old_k = new int[10];

    static char new_energy;
    static char new_pitch;
    static int[] new_k = new int[10];


    /* these are all used to contain the current state of the sound generation */
    static char current_energy;
    static char current_pitch;
    static int[] current_k = new int[10];

    static char target_energy;
    static char target_pitch;
    static int[] target_k = new int[10];

    static char /*UINT8*/ u8_interp_count;
    /* number of interp periods (0-7) */
    static char /*UINT8*/ u8_sample_count;
    /* sample number within interp (0-24) */
    static int pitch_count;

    static int[] u = new int[11];
    static int[] x = new int[10];

    static byte randbit;

    static int DEBUG_5110 = 0;

    /**
     * ********************************************************************************************
     * tms5110_reset -- resets the TMS5110
     * *********************************************************************************************
     */
    public static void tms5110_reset() {
        /* initialize the FIFO */
        memset(u8_fifo, 0, sizeof(u8_fifo));
        u8_fifo_head = u8_fifo_tail = u8_fifo_count = 0;

        /* initialize the chip state */
        u8_speaking_now = u8_speak_delay_frames = u8_talk_status = 0;
        u8_CTL_pins = 0;

        /* initialize the energy/pitch/k states */
        old_energy = new_energy = current_energy = target_energy = 0;
        old_pitch = new_pitch = current_pitch = target_pitch = 0;
        memset(old_k, 0, sizeof(old_k));
        memset(new_k, 0, sizeof(new_k));
        memset(current_k, 0, sizeof(current_k));
        memset(target_k, 0, sizeof(target_k));

        /* initialize the sample generators */
        u8_interp_count = 0;
        u8_sample_count = 0;
        pitch_count = 0;
        randbit = 0;
        memset(u, 0, sizeof(u));
        memset(x, 0, sizeof(x));
    }

    /**
     * ****************************************************************************************
     * tms5110_set_M0_callback -- set M0 callback for the TMS5110
     * ****************************************************************************************
     */
    public static void tms5110_set_M0_callback(M0_callbackPtr func) {
        M0_callback = func;
    }

    /**
     * ****************************************************************************************
     * FIFO_data_write -- handle bit data write to the TMS5110 (as a result of
     * toggling M0 pin)
     * ****************************************************************************************
     */
    public static void FIFO_data_write(int data) {
        /* add this byte to the FIFO */
        if (u8_fifo_count < FIFO_SIZE) {
            u8_fifo[u8_fifo_tail] = (char) ((data & 1) & 0xFF);
            /* set bit to 1 or 0 */

            u8_fifo_tail = (char) (((u8_fifo_tail + 1) % FIFO_SIZE) & 0xFF);
            u8_fifo_count++;

            if (DEBUG_5110 != 0) {
                logerror("Added bit to FIFO (size=%2d)\n", (int) u8_fifo_count);
            }
        } else {
            if (DEBUG_5110 != 0) {
                logerror("Ran out of room in the FIFO!\n");
            }
        }
    }

    /**
     * ****************************************************************************************
     * extract_bits -- extract a specific number of bits from the FIFO
     * ****************************************************************************************
     */
    public static int extract_bits(int count) {
        int val = 0;

        while (count-- != 0) {
            val = (val << 1) | (u8_fifo[u8_fifo_head] & 1);
            u8_fifo_count--;
            u8_fifo_head = (char) (((u8_fifo_head + 1) % FIFO_SIZE) & 0xFF);
        }
        return val;
    }

    public static void request_bits(int no) {
        int i;
        for (i = 0; i < no; i++) {
            if (M0_callback != null) {
                int data = M0_callback.handler();
                FIFO_data_write(data);
            } else if (DEBUG_5110 != 0) {
                logerror("-.ERROR: TMS5110 missing M0 callback function\n");
            }
        }
    }

    public static void perform_dummy_read() {
        if (M0_callback != null) {
            int data = M0_callback.handler();
            if (DEBUG_5110 != 0) {
                logerror("TMS5110 performing dummy read; value read = %1i\n", data & 1);
            }
        } else if (DEBUG_5110 != 0) {
            logerror("-.ERROR: TMS5110 missing M0 callback function\n");
        }
    }

    /**
     * ********************************************************************************************
     * tms5110_status_read -- read status from the TMS5110
     * <p>
     * bit 0 = TS - Talk Status is active (high) when the VSP is processing
     * speech data. Talk Status goes active at the initiation of a SPEAK
     * command. It goes inactive (low) when the stop code (Energy=1111) is
     * processed, or immediately(?????? not TMS5110) by a RESET command. TMS5110
     * datasheets mention this is only available as a result of executing TEST
     * TALK command.
     * *********************************************************************************************
     */
    public static int tms5110_status_read() {

        if (DEBUG_5110 != 0) {
            logerror("Status read: TS=%d\n", (int) u8_talk_status);
        }

        return (u8_talk_status << 0);
        /*CTL1 = still talking ? */
    }

    /**
     * ********************************************************************************************
     * tms5110_ready_read -- returns the ready state of the TMS5110
     * *********************************************************************************************
     */
    public static int tms5110_ready_read() {
        return (u8_fifo_count < FIFO_SIZE - 1) ? 1 : 0;
    }

    /**
     * ********************************************************************************************
     * tms5110_process -- fill the buffer with a specific number of samples
     * *********************************************************************************************
     */
    public static void tms5110_process(ShortPtr buffer, /*unsigned*/ int size) {
        int buf_count = 0;
        int i, interp_period;

        /* tryagain: */
 /* if we're not speaking, fill with nothingness */
        if (u8_speaking_now == 0) {
            //goto empty;
            while (size > 0) {
                buffer.write(buf_count, (short) 0x00);
                buf_count++;
                size--;
            }
            return;
        }
        /* if we're to speak, but haven't started */
        if (u8_talk_status == 0) {

            /*"perform dummy read" is not mentioned in the datasheet */
 /* However Bagman speech roms data are organized in such way so bit at address 0
	** is NOT a speech data. Bit at address 1 is data speech. Seems that the
	** tms5110 is performing dummy read before starting to execute a SPEAK command.
             */
            perform_dummy_read();

            /* parse but don't remove the first frame, and set the status to 1 */
            parse_frame(1);
            u8_talk_status = 1;
        }

        /* loop until the buffer is full or we've stopped speaking */
        while ((size > 0) && u8_speaking_now != 0) {
            int current_val;

            /* if we're ready for a new frame */
            if ((u8_interp_count == 0) && (u8_sample_count == 0)) {
                /* Parse a new frame */
                if (parse_frame(1) == 0) {
                    break;
                }

                /* Set old target as new start of frame */
                current_energy = old_energy;
                current_pitch = old_pitch;
                for (i = 0; i < 10; i++) {
                    current_k[i] = old_k[i];
                }

                /* is this a zero energy frame? */
                if (current_energy == 0) {
                    /*logerror("processing frame: zero energy\n");*/
                    target_energy = 0;
                    target_pitch = current_pitch;
                    for (i = 0; i < 10; i++) {
                        target_k[i] = current_k[i];
                    }
                } /* is this a stop frame? */ else if (current_energy == (energytable[15] >> 6)) {
                    /*if (DEBUG_5110 != 0) logerror("processing frame: stop frame\n");*/
                    current_energy = (char) (energytable[0] >> 6);
                    target_energy = current_energy;
                    u8_speaking_now = u8_talk_status = 0;
                    u8_interp_count = 0;
                    u8_sample_count = 0;
                    pitch_count = 0;

                    /* try to fetch commands again */
 /*goto tryagain;*/
                } else {
                    /* is this the ramp down frame? */
                    if (new_energy == (energytable[15] >> 6)) {
                        /*logerror("processing frame: ramp down\n");*/
                        target_energy = 0;
                        target_pitch = current_pitch;
                        for (i = 0; i < 10; i++) {
                            target_k[i] = current_k[i];
                        }
                    } /* Reset the step size */ else {
                        /*logerror("processing frame: Normal\n");*/
 /*logerror("*** Energy = %d\n",current_energy);*/
 /*logerror("proc: %d %d\n",last_fbuf_head,fbuf_head);*/

                        target_energy = new_energy;
                        target_pitch = new_pitch;

                        for (i = 0; i < 4; i++) {
                            target_k[i] = new_k[i];
                        }
                        if (current_pitch == 0) {
                            for (i = 4; i < 10; i++) {
                                target_k[i] = current_k[i] = 0;
                            }
                        } else {
                            for (i = 4; i < 10; i++) {
                                target_k[i] = new_k[i];
                            }
                        }
                    }
                }
            } else if (u8_interp_count == 0) {
                /* Update values based on step values */
 /*logerror("\n");*/

                interp_period = u8_sample_count / 25;
                current_energy += (target_energy - current_energy) / interp_coeff[interp_period];
                if (old_pitch != 0) {
                    current_pitch += (target_pitch - current_pitch) / interp_coeff[interp_period];
                }

                /*logerror("*** Energy = %d\n",current_energy);*/
                for (i = 0; i < 10; i++) {
                    current_k[i] += (target_k[i] - current_k[i]) / interp_coeff[interp_period];
                }
            }

            if (old_energy == 0) {
                /* generate silent samples here */
                current_val = 0x00;
            } else if (old_pitch == 0) {
                /* generate unvoiced samples here */
                randbit = (byte) ((rand() % 2) * 2 - 1);
                current_val = (randbit * current_energy) / 4;
            } else {
                /* generate voiced samples here */
                if (pitch_count < sizeof(chirptable)) {
                    current_val = (chirptable[pitch_count] * current_energy) / 256;
                } else {
                    current_val = 0x00;
                }
            }

            /* Lattice filter here */
            u[10] = current_val;

            for (i = 9; i >= 0; i--) {
                u[i] = u[i + 1] - ((current_k[i] * x[i]) / 32768);
            }
            for (i = 9; i >= 1; i--) {
                x[i] = x[i - 1] + ((current_k[i - 1] * u[i - 1]) / 32768);
            }

            x[0] = u[0];

            /* clipping, just like the chip */
            if (u[0] > 511) {
                buffer.write(buf_count, (short) (127 << 8));
            } else if (u[0] < -512) {
                buffer.write(buf_count, (short) (-128 << 8));
            } else {
                buffer.write(buf_count, (short) (u[0] << 6));
            }

            /* Update all counts */
            size--;
            u8_sample_count = (char) (((u8_sample_count + 1) % 200) & 0xFF);

            if (current_pitch != 0) {
                pitch_count = (pitch_count + 1) % current_pitch;
            } else {
                pitch_count = 0;
            }

            u8_interp_count = (char) (((u8_interp_count + 1) % 25) & 0xFF);
            buf_count++;
        }

        //empty:
        while (size > 0) {
            buffer.write(buf_count, (short) 0x00);
            buf_count++;
            size--;
        }
    }

    /**
     * ****************************************************************************************
     * CTL_set -- set CTL pins named CTL1, CTL2, CTL4 and CTL8
     * ****************************************************************************************
     */
    public static void tms5110_CTL_set(int data) {
        u8_CTL_pins = (char) (data & 0xf);
    }

    /**
     * ****************************************************************************************
     * PDC_set -- set Processor Data Clock. Execute CTL_pins command on hi-lo
     * transition.
     * ****************************************************************************************
     */
    public static void tms5110_PDC_set(int data) {
        if (u8_PDC != (data & 0x1)) {
            u8_PDC = (char) (data & 0x1);
            if (u8_PDC == 0) /* toggling 1.0 processes command on CTL_pins */ {
                /* only real commands we handle now are SPEAK and RESET */

                switch (u8_CTL_pins & 0xe) /*CTL1 - don't care*/ {
                    case TMS5110_CMD_SPEAK:
                        u8_speaking_now = 1;
                        /*speak_delay_frames = 10;*/

                        //should FIFO be cleared now ?????
                        break;

                    case TMS5110_CMD_RESET:
                        u8_speaking_now = 0;
                        u8_talk_status = 0;
                        break;

                    default:
                        break;
                }
            }
        }
    }

    /**
     * ****************************************************************************************
     * parse_frame -- parse a new frame's worth of data; returns 0 if not enough
     * bits in buffer
     * ****************************************************************************************
     */
    public static int parse_frame(int removeit) {
        int old_head, old_count;
        int bits, indx, i, rep_flag;

        /* remember previous frame */
        old_energy = new_energy;
        old_pitch = new_pitch;
        for (i = 0; i < 10; i++) {
            old_k[i] = new_k[i];
        }

        /* clear out the new frame */
        new_energy = 0;
        new_pitch = 0;
        for (i = 0; i < 10; i++) {
            new_k[i] = 0;
        }

        /* if the previous frame was a stop frame, don't do anything */
        if (old_energy == (energytable[15] >> 6)) {
            return 1;
        }

        /* remember the original FIFO counts, in case we don't have enough bits */
        old_count = u8_fifo_count;
        old_head = u8_fifo_head;

        /* count the total number of bits available */
        bits = u8_fifo_count;

        /* attempt to extract the energy index */
        bits -= 4;
        if (bits < 0) {
            request_bits(-bits);
            /* toggle M0 to receive needed bits */
            bits = 0;
        }
        indx = extract_bits(4);
        new_energy = (char) (energytable[indx] >> 6);

        /* if the index is 0 or 15, we're done */
        if (indx == 0 || indx == 15) {
            if (DEBUG_5110 != 0) {
                logerror("  (4-bit energy=%d frame)\n", new_energy);
            }

            /* clear fifo if stop frame encountered */
            if (indx == 15) {
                if (DEBUG_5110 != 0) {
                    logerror("  (4-bit energy=%d STOP frame)\n", new_energy);
                }
                u8_fifo_head = u8_fifo_tail = u8_fifo_count = 0;
                removeit = 1;
                u8_speaking_now = u8_talk_status = 0;
            }
            //goto done;
            if (DEBUG_5110 != 0) {
                logerror("Parsed a frame successfully - %d bits remaining\n", bits);
            }
            return 1;
        }

        /* attempt to extract the repeat flag */
        bits -= 1;
        if (bits < 0) {
            request_bits(-bits);
            /* toggle M0 to receive needed bits */
            bits = 0;
        }
        rep_flag = extract_bits(1);

        /* attempt to extract the pitch */
        bits -= 5;
        if (bits < 0) {
            request_bits(-bits);
            /* toggle M0 to receive needed bits */
            bits = 0;
        }
        indx = extract_bits(5);
        new_pitch = (char) (pitchtable[indx] / 256);

        /* if this is a repeat frame, just copy the k's */
        if (rep_flag != 0) {
            for (i = 0; i < 10; i++) {
                new_k[i] = old_k[i];
            }

            if (DEBUG_5110 != 0) {
                logerror("  (10-bit energy=%d pitch=%d rep=%d frame)\n", new_energy, new_pitch, rep_flag);
            }
            //goto done;
            if (DEBUG_5110 != 0) {
                logerror("Parsed a frame successfully - %d bits remaining\n", bits);
            }
            return 1;
        }

        /* if the pitch index was zero, we need 4 k's */
        if (indx == 0) {
            /* attempt to extract 4 K's */
            bits -= 18;
            if (bits < 0) {
                request_bits(-bits);
                /* toggle M0 to receive needed bits */
                bits = 0;
            }
            new_k[0] = k1table[extract_bits(5)];
            new_k[1] = k2table[extract_bits(5)];
            new_k[2] = k3table[extract_bits(4)];
            new_k[3] = k4table[extract_bits(4)];

            if (DEBUG_5110 != 0) {
                logerror("  (28-bit energy=%d pitch=%d rep=%d 4K frame)\n", new_energy, new_pitch, rep_flag);
            }
            //goto done;
            if (DEBUG_5110 != 0) {
                logerror("Parsed a frame successfully - %d bits remaining\n", bits);
            }
            return 1;
        }

        /* else we need 10 K's */
        bits -= 39;
        if (bits < 0) {
            request_bits(-bits);
            /* toggle M0 to receive needed bits */
            bits = 0;
        }
        new_k[0] = k1table[extract_bits(5)];
        new_k[1] = k2table[extract_bits(5)];
        new_k[2] = k3table[extract_bits(4)];
        new_k[3] = k4table[extract_bits(4)];
        new_k[4] = k5table[extract_bits(4)];
        new_k[5] = k6table[extract_bits(4)];
        new_k[6] = k7table[extract_bits(4)];
        new_k[7] = k8table[extract_bits(3)];
        new_k[8] = k9table[extract_bits(3)];
        new_k[9] = k10table[extract_bits(3)];

        if (DEBUG_5110 != 0) {
            logerror("  (49-bit energy=%d pitch=%d rep=%d 10K frame)\n", new_energy, new_pitch, rep_flag);
        }

        //done:
        if (DEBUG_5110 != 0) {
            logerror("Parsed a frame successfully - %d bits remaining\n", bits);
        }
        return 1;

    }
}
