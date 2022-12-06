/*
 * ported to v0.37b7
 * 
 */
package gr.codebb.arcadeflex.v037b7.sound;

import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static common.libc.cstdio.sprintf;
import static gr.codebb.arcadeflex.v036.mame.common.memory_region;
import static gr.codebb.arcadeflex.v036.mame.common.memory_region_length;
import gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.streams.*;
import static arcadeflex.v036.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.sound._2608intfH.MAX_2608;
import gr.codebb.arcadeflex.v037b7.sound._2608intfH.YM2608interface;
import static gr.codebb.arcadeflex.v037b7.sound.fmH.*;
import static gr.codebb.arcadeflex.v037b7.sound.fm.*;

public class _2608intf extends snd_interface {

    /* use FM.C with stream system */
    static int[] stream = new int[MAX_2608];

    static short[] rhythm_buf;

    /* Global Interface holder */
    static YM2608interface intf;

    static Object[][] Timer = new Object[MAX_2608][];

    public _2608intf() {
        this.name = "YM-2608";
        this.sound_num = SOUND_YM2608;
        for (int i = 0; i < MAX_2608; i++) {
            Timer[i] = new Object[2];
        }
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((YM2608interface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((YM2608interface) msound.sound_interface).baseclock;
    }

    /* IRQ Handler */
    public static FM_IRQHANDLER_Ptr IRQHandler = new FM_IRQHANDLER_Ptr() {

        @Override
        public void handler(int n, int irq) {
            if (intf.YM2203_handler == null) {
                return;
            }
            if (intf.YM2203_handler[n] != null) {
                intf.YM2203_handler[n].handler(irq);
            }
        }
    };
    /* Timer overflow callback from timer.c */
    public static TimerCallbackHandlerPtr TimerCallbackHandlerPtr_2608 = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            int n = param & 0x7f;
            int c = param >> 7;

            //	logerror("2608 TimerOver %d\n",c);
            Timer[n][c] = null;
            YM2608TimerOver(n, c);
        }
    };
    /* TimerHandler from fm.c */
    public static FM_TIMERHANDLER_Ptr TimerHandler = new FM_TIMERHANDLER_Ptr() {

        @Override
        public void handler(int n, int c, double count, double stepTime) {
            if (count == 0) {
                /* Reset FM Timer */
                if (Timer[n][c] != null) {
                    //			logerror("2608 TimerReset %d\n",c);
                    timer_remove(Timer[n][c]);
                    Timer[n][c] = null;
                }
            } else {
                /* Start FM Timer */
                double timeSec = (double) count * stepTime;

                if (Timer[n][c] == null) {
                    Timer[n][c] = timer_set(timeSec, (c << 7) | n, TimerCallbackHandlerPtr_2608);
                }
            }
        }
    };

    static void FMTimerInit() {
        int i;

        for (i = 0; i < MAX_2608; i++) {
            Timer[i][0] = Timer[i][1] = null;
        }
    }

    /* update request from fm.c */
    public static void YM2608UpdateRequest(int chip) {
        stream_update(stream[chip], 100);
    }

    @Override
    public int start(MachineSound msound) {
        int i, j;
        int rate = Machine.sample_rate;
        //char buf[YM2608_NUMBUF][40];
        String[] name = new String[YM2608_NUMBUF];
        int mixed_vol;
        int[] vol = new int[YM2608_NUMBUF];
        UBytePtr[] pcmbufa = new UBytePtr[YM2608_NUMBUF];
        int[] pcmsizea = new int[YM2608_NUMBUF];
        int[] rhythm_pos = new int[6 + 1];
        int total_size, r_offset, s_size;

        for (int k = 0; k < YM2610_NUMBUF; k++) {
            pcmbufa[k] = new UBytePtr();
        }
        intf = (YM2608interface) msound.sound_interface;
        if (intf.num > MAX_2608) {
            return 1;
        }

        if (sndintf[SOUND_AY8910].start(msound) != 0) {
            return 1;
        }

        /* Timer Handler set */
        FMTimerInit();

        /* stream system initialize */
        for (i = 0; i < intf.num; i++) {
            /* stream setup */
            mixed_vol = intf.volumeFM[i];
            /* stream setup */
            for (j = 0; j < YM2608_NUMBUF; j++) {
                //name[j]=buf[j];
                vol[j] = mixed_vol & 0xffff;
                mixed_vol >>= 16;
                name[j] = sprintf("%s #%d Ch%d", sound_name(msound), i, j + 1);
            }
            stream[i] = stream_init_multi(YM2608_NUMBUF, name, vol, rate, i, YM2608UpdateOne);
            /* setup adpcm buffers */
            pcmbufa[i] = memory_region(intf.pcmrom[i]);
            pcmsizea[i] = memory_region_length(intf.pcmrom[i]);
        }

        /* rythm rom build */
        rhythm_buf = null;
        /* aloocate rythm data */
        rhythm_buf = new short[6 * 2];
        if (rhythm_buf == null) {
            return 0;
        }
        for (i = 0; i < 6; i++) {
            /* set start point */
            rhythm_pos[i] = i * 2;
            rhythm_buf[i] = 0;
            /* set end point */
            rhythm_pos[i + 1] = (i + 1) * 2;
        }

        /**
         * ** initialize YM2608 ***
         */
        if (YM2608Init(intf.num, intf.baseclock, rate,
                pcmbufa, pcmsizea, rhythm_buf, rhythm_pos,
                TimerHandler, IRQHandler) == 0) {
            return 0;
        }

        /* error */
        return 1;
    }

    @Override
    public void stop() {
        YM2608Shutdown();
        if (rhythm_buf != null) {
            rhythm_buf = null;
        }
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        int i;

        for (i = 0; i < intf.num; i++) {
            YM2608ResetChip(i);
        }
    }
    /**
     * *********************************************
     */
    /* Status Read for YM2608 - Chip 0				*/
    /**
     * *********************************************
     */
    public static ReadHandlerPtr YM2608_status_port_0_A_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            //logerror("PC %04x: 2608 S0A=%02X\n",cpu_get_pc(),YM2608Read(0,0));
            return YM2608Read(0, 0);
        }
    };

    public static ReadHandlerPtr YM2608_status_port_0_B_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            //logerror("PC %04x: 2608 S0B=%02X\n",cpu_get_pc(),YM2608Read(0,2));
            return YM2608Read(0, 2);
        }
    };

    /**
     * *********************************************
     */
    /* Status Read for YM2608 - Chip 1				*/
    /**
     * *********************************************
     */
    public static ReadHandlerPtr YM2608_status_port_1_A_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2608Read(1, 0);
        }
    };

    public static ReadHandlerPtr YM2608_status_port_1_B_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2608Read(1, 2);
        }
    };

    /**
     * *********************************************
     */
    /* Port Read for YM2608 - Chip 0				*/
    /**
     * *********************************************
     */
    public static ReadHandlerPtr YM2608_read_port_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2608Read(0, 1);
        }
    };

    /**
     * *********************************************
     */
    /* Port Read for YM2608 - Chip 1				*/
    /**
     * *********************************************
     */
    public static ReadHandlerPtr YM2608_read_port_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2608Read(1, 1);
        }
    };

    /**
     * *********************************************
     */
    /* Control Write for YM2608 - Chip 0			*/
 /* Consists of 2 addresses						*/
    /**
     * *********************************************
     */
    public static WriteHandlerPtr YM2608_control_port_0_A_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2608Write(0, 0, data);
        }
    };

    public static WriteHandlerPtr YM2608_control_port_0_B_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2608Write(0, 2, data);
        }
    };

    /**
     * *********************************************
     */
    /* Control Write for YM2608 - Chip 1			*/
 /* Consists of 2 addresses						*/
    /**
     * *********************************************
     */
    public static WriteHandlerPtr YM2608_control_port_1_A_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2608Write(1, 0, data);
        }
    };

    public static WriteHandlerPtr YM2608_control_port_1_B_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2608Write(1, 2, data);
        }
    };

    /**
     * *********************************************
     */
    /* Data Write for YM2608 - Chip 0				*/
 /* Consists of 2 addresses						*/
    /**
     * *********************************************
     */
    public static WriteHandlerPtr YM2608_data_port_0_A_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2608Write(0, 1, data);
        }
    };

    public static WriteHandlerPtr YM2608_data_port_0_B_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2608Write(0, 3, data);
        }
    };

    /**
     * *********************************************
     */
    /* Data Write for YM2608 - Chip 1				*/
 /* Consists of 2 addresses						*/
    /**
     * *********************************************
     */
    public static WriteHandlerPtr YM2608_data_port_1_A_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2608Write(1, 1, data);
        }
    };
    public static WriteHandlerPtr YM2608_data_port_1_B_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2608Write(1, 3, data);
        }
    };

    /**
     * ************** end of file ***************
     */
}
