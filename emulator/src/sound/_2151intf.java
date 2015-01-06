package sound;

import mame.sndintrf.*;
import static mame.sndintrfH.*;
import static sound._2151intfH.*;
import static mame.driverH.*;
import static mame.sndintrf.*;
import static arcadeflex.libc_old.*;
import static mame.mame.*;
import static sound.fm.*;
import static sound.fmH.*;
import static sound.streams.*;
import static mame.timer.*;

public class _2151intf extends snd_interface {

    public _2151intf() {
        this.name = "YM-2151";
        this.sound_num = SOUND_YM2151;
        for (int i = 0; i < MAX_2151; i++) {
            Timer[i] = new Object[2];
        }
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((YM2151interface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((YM2151interface) msound.sound_interface).baseclock;
    }

    /* for stream system */
    static int[] stream = new int[MAX_2151];

    static YM2151interface intf;

    public static final int YM2151_NUMBUF = 2;

    static Object[][] Timer = new Object[MAX_2151][];

    /* IRQ Handler */
    public static FM_IRQHANDLEPtr IRQHandler = new FM_IRQHANDLEPtr() {

        @Override
        public void handler(int n, int irq) {
            if (intf.irqhandler == null) {
                return;
            }
            if (intf.irqhandler[n] != null) {
                intf.irqhandler[n].handler(irq);
            }
        }
    };

    public static timer_callback timer_callback_2151 = new timer_callback() {
        public void handler(int param) {
            int n = param & 0x7f;
            int c = param >> 7;

            Timer[n][c] = 0;
            YM2151TimerOver(n, c);
        }
    };

    /* TimerHandler from fm.c */
    public static FM_TIMERHANDLERtr TimerHandler = new FM_TIMERHANDLERtr() {

        @Override
        public void handler(int n, int c, double count, double stepTime) {
            if (count == 0) {	/* Reset FM Timer */

                if (Timer[n][c] != null) {
                    timer_remove(Timer[n][c]);
                    Timer[n][c] = 0;
                }
            } else {	/* Start FM Timer */

                double timeSec = (double) count * stepTime;

                if (Timer[n][c] == null) {
                    Timer[n][c] = timer_set(timeSec, (c << 7) | n, timer_callback_2151);
                }
            }
        }
    };

    static int my_YM2151_sh_start(MachineSound msound, int mode) {
        int i, j;
        int rate = Machine.sample_rate;
        String[] name = new String[YM2151_NUMBUF];
        int mixed_vol;
        int[] vol = new int[YM2151_NUMBUF];

        if (rate == 0) {
            rate = 1000;	/* kludge to prevent nasty crashes */
        }

        intf = (YM2151interface) msound.sound_interface;

        /* stream system initialize */
        for (i = 0; i < intf.num; i++) {
            mixed_vol = intf.volume[i];
            /* stream setup */
            for (j = 0; j < YM2151_NUMBUF; j++) {
                //name[j]=buf[j];
                vol[j] = mixed_vol & 0xffff;
                mixed_vol >>= 16;
                name[j] = sprintf("%s #%d Ch%d", sound_name(msound), i, j + 1);//sprintf(buf[j],"%s #%d Ch%d",sound_name(msound),i,j+1);
            }
            stream[i] = stream_init_multi(YM2151_NUMBUF,
                    name, vol, rate, i, OPMUpdateOne);
        }
        /* Set Timer handler */
        for (i = 0; i < intf.num; i++) {
            Timer[i][0] = Timer[i][1] = 0;
        }
        if (OPMInit(intf.num, intf.baseclock, Machine.sample_rate, TimerHandler, IRQHandler) == 0) {
            /* set port handler */
            for (i = 0; i < intf.num; i++) {
                OPMSetPortHander(i, intf.portwritehandler[i]);
            }
            return 0;
        }
        /* error */
        return 1;
    }

    @Override
    public int start(MachineSound msound) {
        return my_YM2151_sh_start(msound, 0);
    }

    @Override
    public void stop() {
        OPMShutdown();
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        int i;

        for (i = 0; i < intf.num; i++) {
            OPMResetChip(i);
        }
    }

    static int lastreg0, lastreg1, lastreg2;

    public static ReadHandlerPtr YM2151_status_port_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2151Read(0, 1);
        }
    };

    public static ReadHandlerPtr YM2151_status_port_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2151Read(1, 1);
        }
    };

    public static ReadHandlerPtr YM2151_status_port_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2151Read(2, 1);
        }
    };

    public static WriteHandlerPtr YM2151_register_port_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            lastreg0 = data;
        }
    };
    public static WriteHandlerPtr YM2151_register_port_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            lastreg1 = data;
        }
    };
    public static WriteHandlerPtr YM2151_register_port_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            lastreg2 = data;
        }
    };

    public static WriteHandlerPtr YM2151_data_port_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2151Write(0, 0, lastreg0);
            YM2151Write(0, 1, data);
        }
    };

    public static WriteHandlerPtr YM2151_data_port_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2151Write(1, 0, lastreg1);
            YM2151Write(1, 1, data);
        }
    };

    public static WriteHandlerPtr YM2151_data_port_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2151Write(2, 0, lastreg2);
            YM2151Write(2, 1, data);
        }
    };

}
