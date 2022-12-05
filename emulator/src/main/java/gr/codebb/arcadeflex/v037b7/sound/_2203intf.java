/*|*
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.v037b7.sound;

import static common.libc.cstdio.sprintf;
import gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.v037b7.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.sound._2203intfH.*;
import static gr.codebb.arcadeflex.v037b7.sound.fmH.*;
import static gr.codebb.arcadeflex.v037b7.sound.fm.*;

public class _2203intf extends snd_interface {

    static int[] stream = new int[MAX_2203];
    static YM2203interface intf;
    static Object[][] Timer = new Object[MAX_2203][];

    public _2203intf() {
        this.name = "YM-2203";
        this.sound_num = SOUND_YM2203;
        for (int i = 0; i < MAX_2203; i++) {
            Timer[i] = new Object[2];
        }
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((YM2203interface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((YM2203interface) msound.sound_interface).baseclock;
    }

    @Override
    public void update() {
        //NO functionality expected
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
    public static timer_callback timer_callback_2203 = new timer_callback() {
        public void handler(int param) {
            int n = param & 0x7f;
            int c = param >> 7;

            Timer[n][c] = null;
            YM2203TimerOver(n, c);
        }
    };

    /* update request from fm.c */
    public static void YM2203UpdateRequest(int chip) {
        stream_update(stream[chip], 0);
    }

    /* TimerHandler from fm.c */
    public static FM_TIMERHANDLER_Ptr TimerHandler = new FM_TIMERHANDLER_Ptr() {

        @Override
        public void handler(int n, int c, double count, double stepTime) {
            if (count == 0) {
                /* Reset FM Timer */
                if (Timer[n][c] != null) {
                    timer_remove(Timer[n][c]);
                    Timer[n][c] = null;
                }
            } else {
                /* Start FM Timer */
                double timeSec = (double) count * stepTime;

                if (Timer[n][c] == null) {
                    Timer[n][c] = timer_set(timeSec, (c << 7) | n, timer_callback_2203);
                }
            }
        }
    };

    static void FMTimerInit() {
        for (int i = 0; i < MAX_2203; i++) {
            Timer[i][0] = Timer[i][1] = null;
        }
    }

    @Override
    public int start(MachineSound msound) {
        int i;
        if (sndintf[SOUND_AY8910].start(msound) != 0) {
            return 1;
        }

        intf = (YM2203interface) msound.sound_interface;

        /* Timer Handler set */
        FMTimerInit();
        /* stream system initialize */
        for (i = 0; i < intf.num; i++) {
            int volume;
            String name = sprintf("%s #%d FM", sound_name(msound), i);
            volume = intf.mixing_level[i] >> 16;
            /* high 16 bit */

            stream[i] = stream_init(name, volume, Machine.sample_rate, i, YM2203UpdateOne);
        }
        /* Initialize FM emurator */
        if (YM2203Init(intf.num, intf.baseclock, Machine.sample_rate, TimerHandler, IRQHandler) == 0) {
            /* Ready */
            return 0;
        }
        /* error */
 /* stream close */
        return 1;

    }

    @Override
    public void stop() {
        YM2203Shutdown();
    }

    @Override
    public void reset() {
        int i;

        for (i = 0; i < intf.num; i++) {
            YM2203ResetChip(i);
        }

    }

    public static ReadHandlerPtr YM2203_status_port_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2203Read(0, 0);
        }
    };
    public static ReadHandlerPtr YM2203_status_port_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2203Read(1, 0);
        }
    };
    public static ReadHandlerPtr YM2203_status_port_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2203Read(2, 0);
        }
    };
    public static ReadHandlerPtr YM2203_status_port_3_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2203Read(3, 0);
        }
    };
    public static ReadHandlerPtr YM2203_status_port_4_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2203Read(4, 0);
        }
    };

    public static ReadHandlerPtr YM2203_read_port_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2203Read(0, 1);
        }
    };
    public static ReadHandlerPtr YM2203_read_port_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2203Read(1, 1);
        }
    };
    public static ReadHandlerPtr YM2203_read_port_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2203Read(2, 1);
        }
    };
    public static ReadHandlerPtr YM2203_read_port_3_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2203Read(3, 1);
        }
    };
    public static ReadHandlerPtr YM2203_read_port_4_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2203Read(4, 1);
        }
    };

    public static WriteHandlerPtr YM2203_control_port_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2203Write(0, 0, data);
        }
    };
    public static WriteHandlerPtr YM2203_control_port_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2203Write(1, 0, data);
        }
    };
    public static WriteHandlerPtr YM2203_control_port_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2203Write(2, 0, data);
        }
    };
    public static WriteHandlerPtr YM2203_control_port_3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2203Write(3, 0, data);
        }
    };
    public static WriteHandlerPtr YM2203_control_port_4_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2203Write(4, 0, data);
        }
    };

    public static WriteHandlerPtr YM2203_write_port_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2203Write(0, 1, data);
        }
    };
    public static WriteHandlerPtr YM2203_write_port_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2203Write(1, 1, data);
        }
    };
    public static WriteHandlerPtr YM2203_write_port_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2203Write(2, 1, data);
        }
    };
    public static WriteHandlerPtr YM2203_write_port_3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2203Write(3, 1, data);
        }
    };
    public static WriteHandlerPtr YM2203_write_port_4_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2203Write(4, 1, data);
        }
    };

}
