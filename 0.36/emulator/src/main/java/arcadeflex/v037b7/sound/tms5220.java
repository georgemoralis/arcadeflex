/*
 * ported to v0.37b7
 */
/**
 * Changelog
 * =========
 * 02/02/2023 - shadow - This file should be complete for 0.37b7 version
 */
package arcadeflex.v037b7.sound;

//common imports
//sound imports
import static arcadeflex.v037b7.sound._5220intfH.*;
import static arcadeflex.v037b7.sound.tms5220r.*;
//common
import static common.libc.cstdlib.*;
import static common.libc.cstring.*;
//to be organized
import static gr.codebb.arcadeflex.v036.platform.libc_old.sizeof;
import gr.codebb.arcadeflex.common.PtrLib.ShortPtr;

public class tms5220 {

    /* these contain data that describes the 128-bit data FIFO */
    static final int FIFO_SIZE = 16;
    static /*UINT8*/ int[] u8_fifo = new int[FIFO_SIZE];
    static int/*UINT8*/ u8_fifo_head;
    static int/*UINT8*/ u8_fifo_tail;
    static int/*UINT8*/ u8_fifo_count;
    static int/*UINT8*/ u8_bits_taken;


    /* these contain global status bits */
    static int/*UINT8*/ u8_speak_external;
    static int/*UINT8*/ u8_speak_delay_frames;
    static int/*UINT8*/ u8_talk_status;
    static int/*UINT8*/ u8_buffer_low;
    static int/*UINT8*/ u8_buffer_empty;
    static int/*UINT8*/ u8_irq_pin;

    static IrqPtr irq_func;


    /* these contain data describing the current and previous voice frames */
    static int/*UINT16*/ u16_old_energy;
    static int/*UINT16*/ u16_old_pitch;
    static int[] old_k = new int[10];

    static int/*UINT16*/ u16_new_energy;
    static int/*UINT16*/ u16_new_pitch;
    static int[] new_k = new int[10];


    /* these are all used to contain the current state of the sound generation */
    static int/*UINT16*/ u16_current_energy;
    static int/*UINT16*/ u16_current_pitch;
    static int[] current_k = new int[10];

    static int/*UINT16*/ u16_target_energy;
    static int/*UINT16*/ u16_target_pitch;
    static int[] target_k = new int[10];

    static int/*UINT8*/ u8_interp_count;/* number of interp periods (0-7) */
    static int/*UINT8*/ u8_sample_count;/* sample number within interp (0-24) */
    static int pitch_count;

    static int[] u = new int[11];
    static int[] x = new int[10];

    static byte randbit;

    //#define DEBUG_5220	0
    /**
     * ********************************************************************************************
     *
     * tms5220_reset -- resets the TMS5220
     *
     **********************************************************************************************
     */
    public static void tms5220_reset() {
        /* initialize the FIFO */
        memset(u8_fifo, 0, sizeof(u8_fifo));
        u8_fifo_head = u8_fifo_tail = u8_fifo_count = u8_bits_taken = 0;

        /* initialize the chip state */
        u8_speak_external = u8_speak_delay_frames = u8_talk_status = u8_irq_pin = 0;
        u8_buffer_empty = u8_buffer_low = 1;

        /* initialize the energy/pitch/k states */
        u16_old_energy = u16_new_energy = u16_current_energy = u16_target_energy = 0;
        u16_old_pitch = u16_new_pitch = u16_current_pitch = u16_target_pitch = 0;
        memset(old_k, 0, sizeof(old_k));
        memset(new_k, 0, sizeof(new_k));
        memset(current_k, 0, sizeof(current_k));
        memset(target_k, 0, sizeof(target_k));

        /* initialize the sample generators */
        u8_interp_count = u8_sample_count = pitch_count = 0;
        randbit = 0;
        memset(u, 0, sizeof(u));
        memset(x, 0, sizeof(x));
    }

    /**
     * ********************************************************************************************
     * tms5220_reset -- reset the TMS5220
     * *********************************************************************************************
     */
    public static void tms5220_set_irq(IrqPtr func) {
        irq_func = func;
    }

    /**
     * ********************************************************************************************
     *
     * tms5220_data_write -- handle a write to the TMS5220
     *
     **********************************************************************************************
     */
    public static void tms5220_data_write(int data) {
        /* add this byte to the FIFO */
        if (u8_fifo_count < FIFO_SIZE) {
            u8_fifo[u8_fifo_tail] = data & 0xFF;
            u8_fifo_tail = ((u8_fifo_tail + 1) % FIFO_SIZE) & 0xFF;
            u8_fifo_count = (u8_fifo_count + 1) & 0xFF;

            /* if we were speaking, then we're no longer empty */
            if (u8_speak_external != 0) {
                u8_buffer_empty = 0;
            }
            //if (DEBUG_5220) logerror("Added byte to FIFO (size=%2d)\n", fifo_count);
        } else {
            //if (DEBUG_5220) logerror("Ran out of room in the FIFO!\n");
        }

        /* update the buffer low state */
        check_buffer_low();
    }

    /**
     * ********************************************************************************************
     *
     * tms5220_status_read -- read status from the TMS5220
     *
     * From the data sheet: bit 0 = TS - Talk Status is active (high) when the
     * VSP is processing speech data. Talk Status goes active at the initiation
     * of a Speak command or after nine bytes of data are loaded into the FIFO
     * following a Speak External command. It goes inactive (low) when the stop
     * code (Energy=1111) is processed, or immediately by a buffer empty
     * condition or a reset command. bit 1 = BL - Buffer Low is active (high)
     * when the FIFO buffer is more than half empty. Buffer Low is set when the
     * "Last-In" byte is shifted down past the half-full boundary of the stack.
     * Buffer Low is cleared when data is loaded to the stack so that the
     * "Last-In" byte lies above the half-full boundary and becomes the ninth
     * data byte of the stack. bit 2 = BE - Buffer Empty is active (high) when
     * the FIFO buffer has run out of data while executing a Speak External
     * command. Buffer Empty is set when the last bit of the "Last-In" byte is
     * shifted out to the Synthesis Section. This causes Talk Status to be
     * cleared. Speed is terminated at some abnormal point and the Speak
     * External command execution is terminated.
     *
     **********************************************************************************************
     */
    public static int tms5220_status_read() {
        /* clear the interrupt pin */
        set_interrupt_state(0);

        //if (DEBUG_5220) logerror("Status read: TS=%d BL=%d BE=%d\n", talk_status, buffer_low, buffer_empty);
        return (u8_talk_status << 7) | (u8_buffer_low << 6) | (u8_buffer_empty << 5);
    }

    /**
     * ********************************************************************************************
     *
     * tms5220_ready_read -- returns the ready state of the TMS5220
     *
     **********************************************************************************************
     */
    public static int tms5220_ready_read() {
        return (u8_fifo_count < FIFO_SIZE - 1) ? 1 : 0;
    }

    /**
     * ********************************************************************************************
     *
     * tms5220_int_read -- returns the interrupt state of the TMS5220
     *
     **********************************************************************************************
     */
    public static int tms5220_int_read() {
        return u8_irq_pin & 0xFF;
    }

    /**
     * ********************************************************************************************
     *
     * tms5220_process -- fill the buffer with a specific number of samples
     *
     **********************************************************************************************
     */
    static int p_buf_count;
    static int p_interp_period;
    static int p_size;

    public static int tryagain(ShortPtr buffer) {
        while (u8_speak_external == 0 && u8_fifo_count > 0) {
            process_command();
        }
        /* if we're empty and still not speaking, fill with nothingness */
        if (u8_speak_external == 0) {
            return 1;
        }
        /* if we're to speak, but haven't started, wait for the 9th byte */
        if (u8_talk_status == 0) {
            if (u8_fifo_count < 9) {
                return 1;
            }

            /* parse but don't remove the first frame, and set the status to 1 */
            parse_frame(0);
            u8_talk_status = 1;
            u8_buffer_empty = 0;
        }
        /* apply some delay before we actually consume data; Victory requires this */
        if (u8_speak_delay_frames != 0) {
            if (p_size <= u8_speak_delay_frames) {
                u8_speak_delay_frames = (u8_speak_delay_frames - p_size) & 0xFF;
                p_size = 0;
            } else {
                p_size -= u8_speak_delay_frames;
                u8_speak_delay_frames = 0;
            }
        }

        /* loop until the buffer is full or we've stopped speaking */
        while ((p_size > 0) && u8_speak_external != 0) {
            int current_val;

            /* if we're ready for a new frame */
            if ((u8_interp_count == 0) && (u8_sample_count == 0)) {
                /* Parse a new frame */
                if (parse_frame(1) == 0) {
                    break;
                }

                /* Set old target as new start of frame */
                u16_current_energy = u16_old_energy;
                u16_current_pitch = u16_old_pitch;
                for (int i = 0; i < 10; i++) {
                    current_k[i] = old_k[i];
                }

                /* is this a zero energy frame? */
                if (u16_current_energy == 0) {
                    /*printf("processing frame: zero energy\n");*/
                    u16_target_energy = 0;
                    u16_target_pitch = u16_current_pitch;
                    for (int i = 0; i < 10; i++) {
                        target_k[i] = current_k[i];
                    }
                } /* is this a stop frame? */ else if (u16_current_energy == (energytable[15] >> 6)) {
                    /*printf("processing frame: stop frame\n");*/
                    u16_current_energy = energytable[0] >> 6;
                    u16_target_energy = u16_current_energy;
                    u8_speak_external = u8_talk_status = 0;
                    u8_interp_count = u8_sample_count = pitch_count = 0;

                    /* generate an interrupt if necessary */
                    set_interrupt_state(1);

                    /* try to fetch commands again */
                    return tryagain(buffer);//goto tryagain;
                } else {
                    /* is this the ramp down frame? */
                    if (u16_new_energy == (energytable[15] >> 6)) {
                        /*printf("processing frame: ramp down\n");*/
                        u16_target_energy = 0;
                        u16_target_pitch = u16_current_pitch;
                        for (int i = 0; i < 10; i++) {
                            target_k[i] = current_k[i];
                        }
                    } /* Reset the step size */ else {
                        /*printf("processing frame: Normal\n");*/
 /*printf("*** Energy = %d\n",current_energy);*/
 /*printf("proc: %d %d\n",last_fbuf_head,fbuf_head);*/

                        u16_target_energy = u16_new_energy;
                        u16_target_pitch = u16_new_pitch;

                        for (int i = 0; i < 4; i++) {
                            target_k[i] = new_k[i];
                        }
                        if (u16_current_pitch == 0) {
                            for (int i = 4; i < 10; i++) {
                                target_k[i] = current_k[i] = 0;
                            }
                        } else {
                            for (int i = 4; i < 10; i++) {
                                target_k[i] = new_k[i];
                            }
                        }
                    }
                }
            } else if (u8_interp_count == 0) {
                /* Update values based on step values */
 /*printf("\n");*/

                p_interp_period = u8_sample_count / 25;
                u16_current_energy = (u16_current_energy + (u16_target_energy - u16_current_energy) / interp_coeff[p_interp_period]) & 0xFFFF;
                if (u16_old_pitch != 0) {
                    u16_current_pitch = (u16_current_pitch + (u16_target_pitch - u16_current_pitch) / interp_coeff[p_interp_period]) & 0xFFFF;
                }

                /*printf("*** Energy = %d\n",current_energy);*/
                for (int i = 0; i < 10; i++) {
                    current_k[i] += (target_k[i] - current_k[i]) / interp_coeff[p_interp_period];
                }
            }

            if (u16_old_energy == 0) {
                /* generate silent samples here */
                current_val = 0x00;
            } else if (u16_old_pitch == 0) {
                /* generate unvoiced samples here */
                randbit = (byte) ((rand() % 2) * 2 - 1);
                current_val = (randbit * u16_current_energy) / 4;
            } else {
                /* generate voiced samples here */
                if (pitch_count < sizeof(chirptable)) {
                    current_val = (chirptable[pitch_count] * u16_current_energy) / 256;
                } else {
                    current_val = 0x00;
                }
            }

            /* Lattice filter here */
            u[10] = current_val;

            for (int i = 9; i >= 0; i--) {
                u[i] = u[i + 1] - ((current_k[i] * x[i]) / 32768);
            }
            for (int i = 9; i >= 1; i--) {
                x[i] = x[i - 1] + ((current_k[i - 1] * u[i - 1]) / 32768);
            }

            x[0] = u[0];

            /* clipping, just like the chip */
            if (u[0] > 511) {
                buffer.write(p_buf_count, (short) (127 << 8));
            } else if (u[0] < -512) {
                buffer.write(p_buf_count, (short) (-128 << 8));
            } else {
                buffer.write(p_buf_count, (short) (u[0] << 6));
            }

            /* Update all counts */
            p_size--;
            u8_sample_count = ((u8_sample_count + 1) % 200) & 0xFF;

            if (u16_current_pitch != 0) {
                pitch_count = (pitch_count + 1) % u16_current_pitch;
            } else {
                pitch_count = 0;
            }

            u8_interp_count = ((u8_interp_count + 1) % 25) & 0xFF;
            p_buf_count++;
        }
        return 0;
    }

    public static void tms5220_process(ShortPtr buffer, /*unsigned*/ int size) {
        p_buf_count = 0;
        p_interp_period = 0;
        p_size = size;

        int result = tryagain(buffer);
        if (result == 1) {
            while (p_size > 0) {
                buffer.write(p_buf_count, (short) 0x00);
                p_buf_count++;
                p_size--;
            }
        }
        while (p_size > 0) {
            buffer.write(p_buf_count, (short) 0x00);
            p_buf_count++;
            p_size--;
        }
    }

    /**
     * ********************************************************************************************
     *
     * process_command -- extract a byte from the FIFO and interpret it as a
     * command
     *
     **********************************************************************************************
     */
    static void process_command() {
        int/*unsigned char*/ cmd;

        /* if there are stray bits, ignore them */
        if (u8_bits_taken != 0) {
            u8_bits_taken = 0;
            u8_fifo_count = (u8_fifo_count - 1) & 0xFF;
            u8_fifo_head = ((u8_fifo_head + 1) % FIFO_SIZE) & 0xFF;
        }

        /* grab a full byte from the FIFO */
        if (u8_fifo_count > 0) {
            cmd = u8_fifo[u8_fifo_head] & 0x70;
            u8_fifo_count = (u8_fifo_count - 1) & 0xFF;
            u8_fifo_head = ((u8_fifo_head + 1) % FIFO_SIZE) & 0xFF;

            /* only real command we handle now is speak external */
            if (cmd == 0x60) {
                u8_speak_external = 1;
                u8_speak_delay_frames = 10;

                /* according to the datasheet, this will cause an interrupt due to a BE condition */
                if (u8_buffer_empty == 0) {
                    u8_buffer_empty = 1;
                    set_interrupt_state(1);
                }
            }
        }

        /* update the buffer low state */
        check_buffer_low();
    }

    /**
     * ********************************************************************************************
     *
     * extract_bits -- extract a specific number of bits from the FIFO
     *
     **********************************************************************************************
     */
    static int extract_bits(int count) {
        int val = 0;

        while (count-- != 0) {
            val = (val << 1) | ((u8_fifo[u8_fifo_head] >> u8_bits_taken) & 1);
            u8_bits_taken = (u8_bits_taken + 1) & 0xFF;
            if (u8_bits_taken >= 8) {
                u8_fifo_count = (u8_fifo_count - 1) & 0xFF;
                u8_fifo_head = ((u8_fifo_head + 1) % FIFO_SIZE) & 0xFF;
                u8_bits_taken = 0;
            }
        }
        return val;
    }

    /**
     * ********************************************************************************************
     *
     * parse_frame -- parse a new frame's worth of data; returns 0 if not enough
     * bits in buffer
     *
     **********************************************************************************************
     */
    static int parse_frame(int removeit) {
        int old_head, old_taken, old_count;
        int bits, indx, i, rep_flag;

        /* remember previous frame */
        u16_old_energy = u16_new_energy;
        u16_old_pitch = u16_new_pitch;
        for (i = 0; i < 10; i++) {
            old_k[i] = new_k[i];
        }

        /* clear out the new frame */
        u16_new_energy = 0;
        u16_new_pitch = 0;
        for (i = 0; i < 10; i++) {
            new_k[i] = 0;
        }

        /* if the previous frame was a stop frame, don't do anything */
        if (u16_old_energy == (energytable[15] >> 6)) {
            return 1;
        }

        /* remember the original FIFO counts, in case we don't have enough bits */
        old_count = u8_fifo_count;
        old_head = u8_fifo_head;
        old_taken = u8_bits_taken;

        /* count the total number of bits available */
        bits = u8_fifo_count * 8 - u8_bits_taken;

        /* attempt to extract the energy index */
        bits -= 4;
        if (bits < 0) {
            //goto ranout;
            //if (DEBUG_5220) logerror("Ran out of bits on a parse!\n");

            /* this is an error condition; mark the buffer empty and turn off speaking */
            u8_buffer_empty = 1;
            u8_talk_status = u8_speak_external = 0;
            u8_fifo_count = u8_fifo_head = u8_fifo_tail = 0;

            /* generate an interrupt if necessary */
            set_interrupt_state(1);
            return 0;
        }
        indx = extract_bits(4);
        u16_new_energy = energytable[indx] >> 6;

        /* if the index is 0 or 15, we're done */
        if (indx == 0 || indx == 15) {
            //if (DEBUG_5220) logerror("  (4-bit energy=%d frame)\n",new_energy);

            /* clear fifo if stop frame encountered */
            if (indx == 15) {
                u8_fifo_head = u8_fifo_tail = u8_fifo_count = u8_bits_taken = 0;
                removeit = 1;
            }
            {
                //goto done;
                //if (DEBUG_5220) logerror("Parsed a frame successfully - %d bits remaining\n", bits);

                /* if we're not to remove this one, restore the FIFO */
                if (removeit == 0) {
                    u8_fifo_count = old_count & 0xFF;
                    u8_fifo_head = old_head & 0xFF;
                    u8_bits_taken = old_taken & 0xFF;
                }

                /* update the buffer_low status */
                check_buffer_low();
                return 1;
            }
        }

        /* attempt to extract the repeat flag */
        bits -= 1;
        if (bits < 0) {
            //goto ranout;
            //if (DEBUG_5220) logerror("Ran out of bits on a parse!\n");

            /* this is an error condition; mark the buffer empty and turn off speaking */
            u8_buffer_empty = 1;
            u8_talk_status = u8_speak_external = 0;
            u8_fifo_count = u8_fifo_head = u8_fifo_tail = 0;

            /* generate an interrupt if necessary */
            set_interrupt_state(1);
            return 0;
        }
        rep_flag = extract_bits(1);

        /* attempt to extract the pitch */
        bits -= 6;
        if (bits < 0) {
            //goto ranout;
            //if (DEBUG_5220) logerror("Ran out of bits on a parse!\n");

            /* this is an error condition; mark the buffer empty and turn off speaking */
            u8_buffer_empty = 1;
            u8_talk_status = u8_speak_external = 0;
            u8_fifo_count = u8_fifo_head = u8_fifo_tail = 0;

            /* generate an interrupt if necessary */
            set_interrupt_state(1);
            return 0;
        }
        indx = extract_bits(6);
        u16_new_pitch = pitchtable[indx] / 256;

        /* if this is a repeat frame, just copy the k's */
        if (rep_flag != 0) {
            for (i = 0; i < 10; i++) {
                new_k[i] = old_k[i];
            }

            //if (DEBUG_5220) logerror("  (11-bit energy=%d pitch=%d rep=%d frame)\n", new_energy, new_pitch, rep_flag);
            //goto done;
            {
                //if (DEBUG_5220) logerror("Parsed a frame successfully - %d bits remaining\n", bits);

                /* if we're not to remove this one, restore the FIFO */
                if (removeit == 0) {
                    u8_fifo_count = old_count & 0xFF;
                    u8_fifo_head = old_head & 0xFF;
                    u8_bits_taken = old_taken & 0xFF;
                }

                /* update the buffer_low status */
                check_buffer_low();
                return 1;
            }
        }

        /* if the pitch index was zero, we need 4 k's */
        if (indx == 0) {
            /* attempt to extract 4 K's */
            bits -= 18;
            if (bits < 0) {
                //goto ranout;
                //if (DEBUG_5220) logerror("Ran out of bits on a parse!\n");

                /* this is an error condition; mark the buffer empty and turn off speaking */
                u8_buffer_empty = 1;
                u8_talk_status = u8_speak_external = 0;
                u8_fifo_count = u8_fifo_head = u8_fifo_tail = 0;

                /* generate an interrupt if necessary */
                set_interrupt_state(1);
                return 0;
            }
            new_k[0] = k1table[extract_bits(5)];
            new_k[1] = k2table[extract_bits(5)];
            new_k[2] = k3table[extract_bits(4)];
            new_k[3] = k4table[extract_bits(4)];

            //if (DEBUG_5220) logerror("  (29-bit energy=%d pitch=%d rep=%d 4K frame)\n", new_energy, new_pitch, rep_flag);
            //goto done;
            {
                //if (DEBUG_5220) logerror("Parsed a frame successfully - %d bits remaining\n", bits);

                /* if we're not to remove this one, restore the FIFO */
                if (removeit == 0) {
                    u8_fifo_count = old_count & 0xFF;
                    u8_fifo_head = old_head & 0xFF;
                    u8_bits_taken = old_taken & 0xFF;
                }

                /* update the buffer_low status */
                check_buffer_low();
                return 1;
            }
        }

        /* else we need 10 K's */
        bits -= 39;
        if (bits < 0) {
            //goto ranout;
            //if (DEBUG_5220) logerror("Ran out of bits on a parse!\n");

            /* this is an error condition; mark the buffer empty and turn off speaking */
            u8_buffer_empty = 1;
            u8_talk_status = u8_speak_external = 0;
            u8_fifo_count = u8_fifo_head = u8_fifo_tail = 0;

            /* generate an interrupt if necessary */
            set_interrupt_state(1);
            return 0;
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

        //if (DEBUG_5220) logerror("  (50-bit energy=%d pitch=%d rep=%d 10K frame)\n", new_energy, new_pitch, rep_flag);
        {//done
            //if (DEBUG_5220) logerror("Parsed a frame successfully - %d bits remaining\n", bits);

            /* if we're not to remove this one, restore the FIFO */
            if (removeit == 0) {
                u8_fifo_count = old_count & 0xFF;
                u8_fifo_head = old_head & 0xFF;
                u8_bits_taken = old_taken & 0xFF;
            }

            /* update the buffer_low status */
            check_buffer_low();
            return 1;
        }
    }

    /**
     * ********************************************************************************************
     *
     * check_buffer_low -- check to see if the buffer low flag should be on or
     * off
     *
     **********************************************************************************************
     */
    static void check_buffer_low() {
        /* did we just become low? */
        if (u8_fifo_count <= 8) {
            /* generate an interrupt if necessary */
            if (u8_buffer_low == 0) {
                set_interrupt_state(1);
            }
            u8_buffer_low = 1;

            //if (DEBUG_5220) logerror("Buffer low set\n");
        } /* did we just become full? */ else {
            u8_buffer_low = 0;

            //if (DEBUG_5220) logerror("Buffer low cleared\n");
        }
    }

    /**
     * ********************************************************************************************
     *
     * set_interrupt_state -- generate an interrupt
     *
     **********************************************************************************************
     */
    static void set_interrupt_state(int state) {
        if (irq_func != null && state != u8_irq_pin) {
            irq_func.handler(state);
        }
        u8_irq_pin = state & 0xFF;
    }

}
