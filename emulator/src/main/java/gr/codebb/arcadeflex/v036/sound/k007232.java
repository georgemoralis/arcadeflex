package gr.codebb.arcadeflex.v036.sound;

import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.k007232H.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.platform.libc_v2.*;
public class k007232 extends snd_interface {

    public static final int KDAC_A_PCM_MAX = (2);		/* Channels per chip */

    static kdacApcm[] kpcm = new kdacApcm[MAX_K007232];
    static int[] pcm_chan = new int[MAX_K007232];
    static K007232_interface intf;
    static float[] fncode = new float[0x200];
    public static final int BASE_SHIFT = 12;

    public k007232() {
        this.name = "007232";
        this.sound_num = SOUND_K007232;
        for (int i = 0; i < MAX_K007232; i++) {
            kpcm[i] = new kdacApcm();
        }
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((K007232_interface) msound.sound_interface).num_chips;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return 0;
    }

    @Override
    public int start(MachineSound msound) {
        int i, j;

        intf = (K007232_interface) msound.sound_interface;

        /* Set up the chips */
        for (j = 0; j < intf.num_chips; j++) {
            //char buf[2][40];
            String[] name = new String[2];
            int[] vol = new int[2];
            kpcm[j] = new kdacApcm();
            kpcm[j].pcmbuf[0] = memory_region(intf.bank[j]);
            kpcm[j].pcmbuf[1] = memory_region(intf.bank[j]);
            for (i = 0; i < KDAC_A_PCM_MAX; i++) {
                kpcm[j].start[i] = 0;
                kpcm[j].step[i] = 0;
                kpcm[j].play[i] = 0;
                kpcm[j].loop[i] = 0;
            }
            kpcm[j].vol[0][0] = 255;	/* channel A output to output A */

            kpcm[j].vol[0][1] = 0;
            kpcm[j].vol[1][0] = 0;
            kpcm[j].vol[1][1] = 255;	/* channel B output to output B */

            for (i = 0; i < 0x10; i++) {
                kpcm[j].wreg[i] = 0;
            }

            for (i = 0; i < 2; i++) {
                name[i] = sprintf("007232 #%d Ch %c", j, 'A' + i);
            }
            vol[0] = intf.volume[j] & 0xffff;
            vol[1] = intf.volume[j] >> 16;

            pcm_chan[j] = stream_init_multi(2, name, vol, Machine.sample_rate, j, KDAC_A_update);
        }

        KDAC_A_make_fncode();

        return 0;
    }

    @Override
    public void stop() {
        //no functionality expected
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }

    static void KDAC_A_make_fncode() {
        for (int i = 0; i < 0x200; i++) {
            fncode[i] = (0x200 * 55) / (0x200 - i);
            //    if (errorlog) fprintf( errorlog,"2 : fncode[%04x] = %.2f\n", i, fncode[i] );
        }
    }
    public static StreamInitMultiPtr KDAC_A_update = new StreamInitMultiPtr() {
        public void handler(int chip, ShortPtr[] buffer, int buffer_len) {
            int i;

            ///memset(buffer[0],0,buffer_len * sizeof(INT16));
            //memset(buffer[1],0,buffer_len * sizeof(INT16));
            for (i = 0; i < buffer_len; i++) {
                buffer[0].write(i, (short) 0);
                buffer[1].write(i, (short) 0);
            }

            for (i = 0; i < KDAC_A_PCM_MAX; i++) {
                if (kpcm[chip].play[i] != 0) {
                    int volA, volB, j, out;
                    /*unsigned int*/
                    long addr, old_addr;

                    /**
                     * ** PCM setup ***
                     */
                    addr = kpcm[chip].start[i] + ((kpcm[chip].addr[i] >> BASE_SHIFT) & 0x000fffff);
                    volA = 2 * kpcm[chip].vol[i][0];
                    volB = 2 * kpcm[chip].vol[i][1];
                    for (j = 0; j < buffer_len; j++) {
                        old_addr = addr;
                        addr = kpcm[chip].start[i] + ((kpcm[chip].addr[i] >> BASE_SHIFT) & 0x000fffff);
                        while (old_addr <= addr) {
                            if ((kpcm[chip].pcmbuf[i].read((int) old_addr) & 0x80) != 0) {
                                /* end of sample */

                                if (kpcm[chip].loop[i] != 0) {
                                    /* loop to the beginning */
                                    addr = kpcm[chip].start[i];
                                    kpcm[chip].addr[i] = 0;
                                } else {
                                    /* stop sample */
                                    kpcm[chip].play[i] = 0;
                                }
                                break;
                            }

                            old_addr++;
                        }

                        if (kpcm[chip].play[i] == 0) {
                            break;
                        }

                        kpcm[chip].addr[i] += kpcm[chip].step[i];

                        /*out = (kpcm[chip].pcmbuf[i][addr] & 0x7f) - 0x40;

                         buffer[0][j] += out * volA;
                         buffer[1][j] += out * volB;*/
                        out = (kpcm[chip].pcmbuf[i].read((int) addr) & 0x7f) - 0x40;

                        buffer[0].write(j, (short) (buffer[0].read(j) + out * volA));
                        buffer[1].write(j, (short) (buffer[1].read(j) + out * volB));
                    }
                }
            }
        }
    };

    public static void K007232_set_volume(int chip, int channel, int volumeA, int volumeB) {
        kpcm[chip].vol[channel][0] = volumeA & 0xFF;
        kpcm[chip].vol[channel][1] = volumeB & 0xFF;
    }

    public static void K007232_bankswitch(int chip, UBytePtr ptr_A, UBytePtr ptr_B) {
        kpcm[chip].pcmbuf[0] = ptr_A;
        kpcm[chip].pcmbuf[1] = ptr_B;
    }

    static int K007232_ReadReg(int r, int chip) {
        if (r == 0x05) {
            if (kpcm[chip].start[0] < 0x20000) {
                kpcm[chip].play[0] = 1;
                kpcm[chip].addr[0] = 0;
            }
        } else if (r == 0x0b) {
            if (kpcm[chip].start[1] < 0x20000) {
                kpcm[chip].play[1] = 1;
                kpcm[chip].addr[1] = 0;
            }
        }
        return 0;
    }

    static void K007232_WriteReg(int r, int v, int chip) {
        int data;

        if (Machine.sample_rate == 0) {
            return;
        }
        stream_update(pcm_chan[chip], 0);

        kpcm[chip].wreg[r] = v & 0xFF;			/* stock write data */

        if (r == 0x05) {
            if (kpcm[chip].start[0] < 0x20000) {
                kpcm[chip].play[0] = 1;
                kpcm[chip].addr[0] = 0;
            }
        } else if (r == 0x0b) {
            if (kpcm[chip].start[1] < 0x20000) {
                kpcm[chip].play[1] = 1;
                kpcm[chip].addr[1] = 0;
            }
        } else if (r == 0x0d) {
            /* select if sample plays once or looped */
            kpcm[chip].loop[0] = v & 0x01;
            kpcm[chip].loop[1] = v & 0x02;
            return;
        } else if (r == 0x0c) {
            /* external port, usually volume control */
            if (intf.portwritehandler[chip] != null) {
                intf.portwritehandler[chip].handler(v);
            }
            return;
        } else {
            int reg_port;

            reg_port = 0;
            if (r >= 0x06) {
                reg_port = 1;
                r -= 0x06;
            }
            switch (r) {
                case 0x00:
                case 0x01:
                    /**
                     * ** address step ***
                     */
                    //data = (((((unsigned int)kpcm[chip].wreg[reg_port*0x06 + 0x01])<<8)&0x0100) | (((unsigned int)kpcm[chip].wreg[reg_port*0x06 + 0x00])&0x00ff));
                    data = (int) (((((long) kpcm[chip].wreg[reg_port * 0x06 + 0x01]) << 8) & 0x0100) | (((long) kpcm[chip].wreg[reg_port * 0x06 + 0x00]) & 0x00ff));

                    /*kpcm[chip].step[reg_port] =
                     ( (7850.0 / (float)Machine->sample_rate) ) *
                     ( fncode[data] / (440.00/2) ) *
                     ( (float)3580000 / (float)4000000 ) *
                     (1<<BASE_SHIFT);*/
                    kpcm[chip].step[reg_port] = (long) (((7850.0 / (float) Machine.sample_rate))
                            * (fncode[data] / (440.00 / 2))
                            * ((float) 3580000 / (float) 4000000)
                            * (1 << BASE_SHIFT)) & 0xFFFFFFFFL;
                    break;

                case 0x02:
                case 0x03:
                case 0x04:
                    /**
                     * ** start address ***
                     */
                    /*kpcm[chip].start[reg_port]
                     = ((((unsigned int)
                     kpcm[chip].wreg[reg_port * 0x06 + 0x04] << 16
                     )&0x00010000) |
                     (((unsigned int
                     )kpcm[chip].wreg[reg_port * 0x06 + 0x03] << 8
                     )&0x0000ff00) |
                     (((unsigned int
                     )kpcm[chip].wreg[reg_port * 0x06 + 0x02]    )&0x000000ff));*/
                    kpcm[chip].start[reg_port]
                            = ((((long) kpcm[chip].wreg[reg_port * 0x06 + 0x04] << 16) & 0x00010000)
                            | (((long) kpcm[chip].wreg[reg_port * 0x06 + 0x03] << 8) & 0x0000ff00)
                            | (((long) kpcm[chip].wreg[reg_port * 0x06 + 0x02]) & 0x000000ff));
                    break;
            }
        }
    }
    public static ReadHandlerPtr K007232_read_port_0_r = new ReadHandlerPtr() {
        public int handler(int r) {
            return K007232_ReadReg(r, 0);
        }
    };
    public static ReadHandlerPtr K007232_read_port_1_r = new ReadHandlerPtr() {
        public int handler(int r) {
            return K007232_ReadReg(r, 1);
        }
    };
    public static ReadHandlerPtr K007232_read_port_2_r = new ReadHandlerPtr() {
        public int handler(int r) {
            return K007232_ReadReg(r, 2);
        }
    };
    public static WriteHandlerPtr K007232_write_port_0_w = new WriteHandlerPtr() {
        public void handler(int r, int v) {
            K007232_WriteReg(r, v, 0);
        }
    };
    public static WriteHandlerPtr K007232_write_port_1_w = new WriteHandlerPtr() {
        public void handler(int r, int v) {
            K007232_WriteReg(r, v, 1);
        }
    };
    public static WriteHandlerPtr K007232_write_port_2_w = new WriteHandlerPtr() {
        public void handler(int r, int v) {
            K007232_WriteReg(r, v, 2);
        }
    };
}
