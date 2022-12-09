/*
 Taito 8741 emulation

 1.comminucation main and sub cpu
 2.dipswitch and key handling

 program types

 */

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.machine;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.machine.tait8741H.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;

public class tait8741 {

	//#define __log__ 1
    public static final int CMD_IDLE = 0;
    public static final int CMD_08 = 1;
    public static final int CMD_4a = 2;

    public static class I8741 {
        /*unsigned*/ char toData;    /* to host data      */
        /*unsigned*/ char fromData;  /* from host data    */
        /*unsigned*/ char fromCmd;   /* from host command */
        /*unsigned*/ char status;    /* b0 = rd ready,b1 = wd full,b2 = cmd ?? */
        /*unsigned*/ char mode;
        /*unsigned*/ char phase;
        /*unsigned*/ char[] txd = new char[8];
        /*unsigned*/ char[] rxd = new char[8];
        /*unsigned*/ char parallelselect;
        /*unsigned*/ char txpoint;
        int connect;
        /*unsigned*/ char pending4a;
        int serial_out;
        int coins;
        ReadHandlerPtr portHandler;
    }

    static TAITO8741interface intf;
    //static I8741 *taito8741;
    static I8741[] taito8741 = new I8741[MAX_TAITO8741];

    /* for host data , write */
    static void taito8741_hostdata_w(I8741 st, int data) {
        st.toData = (char) (data & 0xFF);
        st.status |= 0x01;
    }

    /* from host data , read */
    static int taito8741_hostdata_r(I8741 st) {
        if ((st.status & 0x02) == 0) {
            return -1;
        }
        st.status &= 0xfd;
        return st.fromData;
    }

    /* from host command , read */
    static int taito8741_hostcmd_r(I8741 st) {
        if ((st.status & 0x04) == 0) {
            return -1;
        }
        st.status &= 0xfb;
        return st.fromCmd;
    }

    /* TAITO8741 I8741 emulation */
    static void taito8741_serial_rx(I8741 st, char[] data) {
        memcpy(st.rxd, data, 8);
    }

    /* timer callback of serial tx finish */
    public static TimerCallbackHandlerPtr taito8741_serial_tx = new TimerCallbackHandlerPtr() {
        public void handler(int num) {
            I8741 st = taito8741[num];
            I8741 sst;

            if (st.mode == TAITO8741_MASTER) {
                st.serial_out = 1;
            }

            st.txpoint = 1;
            if (st.connect >= 0) {
                sst = taito8741[st.connect];
                /* transfer data */
                taito8741_serial_rx(sst, st.txd);
                /*#if __log__
                 if (errorlog != 0) fprintf(errorlog,"8741-%d Serial data TX to %d\n",num,st.connect);
                 #endif*/
                if (sst.mode == TAITO8741_SLAVE) {
                    sst.serial_out = 1;
                }
            }
        }
    };

    public static void TAITO8741_reset(int num) {
        I8741 st = taito8741[num];
        st.status = 0x00;
        st.phase = 0;
        st.parallelselect = 0;
        st.txpoint = 1;
        st.pending4a = 0;
        st.serial_out = 0;
        st.coins = 0;
        memset(st.rxd, 0, 8);
        memset(st.txd, 0, 8);
    }

    /* 8741 update */
    static void taito8741_update(int num) {
        I8741 st;
        I8741 sst;
        int next = num;
        int data;

        do {
            num = next;
            st = taito8741[num];
            if (st.connect != -1) {
                sst = taito8741[st.connect];
            } else {
                sst = null;
            }
            next = -1;
            /* check pending command */
            switch (st.phase) {
                case CMD_08: /* serial data latch */

                    if (st.serial_out != 0) {
                        st.status &= 0xfb; /* patch for gsword */

                        st.phase = CMD_IDLE;
                        next = num; /* continue this chip */

                    }
                    break;
                case CMD_4a: /* wait for syncronus ? */

                    if (st.pending4a == 0) {
                        taito8741_hostdata_w(st, 0);
                        st.phase = CMD_IDLE;
                        next = num; /* continue this chip */

                    }
                    break;
                case CMD_IDLE:
                    /* ----- data in port check ----- */
                    data = taito8741_hostdata_r(st);
                    if (data != -1) {
                        switch (st.mode) {
                            case TAITO8741_MASTER:
                            case TAITO8741_SLAVE:
                                /* buffering transmit data */
                                if (st.txpoint < 8) {
                                    //if(errorlog && st.txpoint == 0 && num==1 && data&0x80) fprintf(errorlog,"Coin Put\n");
                                    st.txd[st.txpoint++] = (char) (data & 0xFF);
                                }
                                break;
                            case TAITO8741_PORT:
                                if ((data & 0xf8) != 0) { /* ?? */

                                } else { /* port select */

                                    st.parallelselect = (char) ((data & 0x07) & 0xFF);
                                    taito8741_hostdata_w(st, st.portHandler != null ? st.portHandler.handler(st.parallelselect) : 0);
                                }
                        }
                    }
                    /* ----- new command fetch ----- */
                    data = taito8741_hostcmd_r(st);
                    switch (data) {
                        case -1: /* no command data */

                            break;
                        case 0x00: /* read from parallel port */

                            taito8741_hostdata_w(st, st.portHandler != null ? st.portHandler.handler(0) : 0);
                            break;
                        case 0x01: /* read receive buffer 0 */

                        case 0x02: /* read receive buffer 1 */

                        case 0x03: /* read receive buffer 2 */

                        case 0x04: /* read receive buffer 3 */

                        case 0x05: /* read receive buffer 4 */

                        case 0x06: /* read receive buffer 5 */

                        case 0x07: /* read receive buffer 6 */

                            //if(errorlog && data == 2 && num==0 && st.rxd[data-1]&0x80) fprintf(errorlog,"Coin Get\n");

                            taito8741_hostdata_w(st, st.rxd[data - 1]);
                            break;
                        case 0x08:	/* latch received serial data */

                            st.txd[0] = (char) (st.portHandler != null ? st.portHandler.handler(0) : 0);
                            if (sst != null) {
                                timer_set(TIME_NOW, num, taito8741_serial_tx);
                                st.serial_out = 0;
                                st.status |= 0x04;
                                st.phase = CMD_08;
                            }
                            break;
                        case 0x0a:	/* 8741-0 : set serial comminucation mode 'MASTER' */

                            //st.mode = TAITO8741_MASTER;

                            break;
                        case 0x0b:	/* 8741-1 : set serial comminucation mode 'SLAVE'  */

                            //st.mode = TAITO8741_SLAVE;

                            break;
                        case 0x1f:  /* 8741-2,3 : ?? set parallelport mode ?? */

                        case 0x3f:  /* 8741-2,3 : ?? set parallelport mode ?? */

                        case 0xe1:  /* 8741-2,3 : ?? set parallelport mode ?? */

                            st.mode = TAITO8741_PORT;
                            st.parallelselect = 1; /* preset read number */

                            break;
                        case 0x62:  /* 8741-3   : ? */

                            break;
                        case 0x4a:	/* ?? syncronus with other cpu and return 00H */

                            if (sst != null) {
                                if (sst.pending4a != 0) {
                                    sst.pending4a = 0; /* syncronus */

                                    taito8741_hostdata_w(st, 0); /* return for host */

                                    next = st.connect;
                                } else {
                                    st.phase = CMD_4a;
                                }
                            }
                            break;
                        case 0x80:	/* 8741-3 : return check code */

                            taito8741_hostdata_w(st, 0x66);
                            break;
                        case 0x81:	/* 8741-2 : return check code */

                            taito8741_hostdata_w(st, 0x48);
                            break;
                        case 0xf0:  /* GSWORD 8741-1 : initialize ?? */

                            break;
                        case 0x82:  /* GSWORD 8741-2 unknown */

                            break;
                    }
                    break;
            }
        } while (next >= 0);
    }

    public static int TAITO8741_start(TAITO8741interface taito8741intf) {
        int i;

        intf = taito8741intf;

		//taito8741 = (I8741 *)malloc(intf.num*sizeof(I8741));
        //if( taito8741 == 0 ) return 1;
        for (i = 0; i < intf.num; i++) {
            taito8741[i] = new I8741();
            taito8741[i].connect = intf.serial_connect[i];
            taito8741[i].portHandler = intf.portHandler_r[i];
            taito8741[i].mode = (char) (intf.mode[i] & 0xFF);
            TAITO8741_reset(i);
        }
        return 0;
    }

    /* read status port */
    public static ReadHandlerPtr I8741_status_r = new ReadHandlerPtr() {
        public int handler(int num) {
            I8741 st = taito8741[num];
            taito8741_update(num);
            /*#if __log__
             if (errorlog != 0) fprintf(errorlog,"8741-%d ST Read %02x PC=%04x\n",num,st.status,cpu_get_pc());
             #endif*/
            return st.status;
        }
    };

    /* read data port */
    public static ReadHandlerPtr I8741_data_r = new ReadHandlerPtr() {
        public int handler(int num) {
            I8741 st = taito8741[num];
            int ret = st.toData;
            st.status &= 0xfe;
            /*#if __log__
             if (errorlog != 0) fprintf(errorlog,"8741-%d DATA Read %02x PC=%04x\n",num,ret,cpu_get_pc());
             #endif*/
            /* update chip */
            taito8741_update(num);

            switch (st.mode) {
                case TAITO8741_PORT: /* parallel data */

                    taito8741_hostdata_w(st, st.portHandler != null ? st.portHandler.handler(st.parallelselect) : 0);
                    break;
            }
            return ret;
        }
    };

    /* Write data port */
    public static WriteHandlerPtr I8741_data_w = new WriteHandlerPtr() {
        public void handler(int num, int data) {
            I8741 st = taito8741[num];
            /*#if __log__
             if (errorlog != 0) fprintf(errorlog,"8741-%d DATA Write %02x PC=%04x\n",num,data,cpu_get_pc());
             #endif*/
            st.fromData = (char) (data & 0xFF);
            st.status |= 0x02;
            /* update chip */
            taito8741_update(num);
        }
    };

    /* Write command port */
    public static WriteHandlerPtr I8741_command_w = new WriteHandlerPtr() {
        public void handler(int num, int data) {
            I8741 st = taito8741[num];
            /*#if __log__
             if (errorlog != 0) fprintf(errorlog,"8741-%d CMD Write %02x PC=%04x\n",num,data,cpu_get_pc());
             #endif*/
            st.fromCmd = (char) (data & 0xFF);
            st.status |= 0x04;
            /* update chip */
            taito8741_update(num);
        }
    };

    /* Write port handler */
    public static WriteHandlerPtr TAITO8741_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((offset & 1) != 0) {
                I8741_command_w.handler(0, data);
            } else {
                I8741_data_w.handler(0, data);
            }
        }
    };
    public static WriteHandlerPtr TAITO8741_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((offset & 1) != 0) {
                I8741_command_w.handler(1, data);
            } else {
                I8741_data_w.handler(1, data);
            }
        }
    };
    public static WriteHandlerPtr TAITO8741_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((offset & 1) != 0) {
                I8741_command_w.handler(2, data);
            } else {
                I8741_data_w.handler(2, data);
            }
        }
    };
    public static WriteHandlerPtr TAITO8741_3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((offset & 1) != 0) {
                I8741_command_w.handler(3, data);
            } else {
                I8741_data_w.handler(3, data);
            }
        }
    };

    /* Read port handler */
    public static ReadHandlerPtr TAITO8741_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if ((offset & 1) != 0) {
                return I8741_status_r.handler(0);
            }
            return I8741_data_r.handler(0);
        }
    };
    public static ReadHandlerPtr TAITO8741_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if ((offset & 1) != 0) {
                return I8741_status_r.handler(1);
            }
            return I8741_data_r.handler(1);
        }
    };
    public static ReadHandlerPtr TAITO8741_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if ((offset & 1) != 0) {
                return I8741_status_r.handler(2);
            }
            return I8741_data_r.handler(2);
        }
    };
    public static ReadHandlerPtr TAITO8741_3_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if ((offset & 1) != 0) {
                return I8741_status_r.handler(3);
            }
            return I8741_data_r.handler(3);
        }
    };
}
