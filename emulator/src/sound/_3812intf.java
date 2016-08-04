package sound;

import mame.sndintrfH;
import static mame.sndintrfH.*;
import static sound._3812intfH.*;
import static mame.driverH.*;
import static mame.mame.*;
import static arcadeflex.libc_old.*;
import static mame.sndintrf.*;
import static sound.streams.*;
import static sound.fmoplH.*;
import static sound.fmopl.*;
import static mame.cpuintrfH.*;
import static mame.timer.*;
import sound.fm_c.FM_OPL;
import static arcadeflex.libc_v2.*;

public class _3812intf extends snd_interface {

    static YM3812interface intf = null;
    static int chiptype;
    static int[] stream = new int[MAX_3812];
    static Object[] Timer = new Object[MAX_3812 * 2];
    static FM_OPL[] F3812 = new FM_OPL[MAX_3812];

    public _3812intf() {
        sound_num = SOUND_YM3812;
        name = "YM-3812";
    }

    @Override
    public int chips_num(sndintrfH.MachineSound msound) {
        return ((YM3812interface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(sndintrfH.MachineSound msound) {
        return ((YM3812interface) msound.sound_interface).baseclock;
    }

    @Override
    public int start(sndintrfH.MachineSound msound) {
        chiptype = OPL_TYPE_YM3812;
        return OPL_sh_start(msound);
    }

    public static int OPL_sh_start(sndintrfH.MachineSound msound) {

        int i;
        int rate = Machine.sample_rate;

        intf = (YM3812interface) msound.sound_interface;
        if (intf.num > MAX_3812) {
            return 1;
        }

        /* Timer state clear */
        for (i = 0; i < Timer.length; i++) {
            Timer[i] = null;//memset(Timer,0,sizeof(Timer));
        }
        /* stream system initialize */
        for (i = 0; i < intf.num; i++) {
            /* stream setup */
            String name;
            int vol = intf.mixing_level[i];
            /* emulator create */
            F3812[i] = OPLCreate(chiptype, intf.baseclock, rate);
            if (F3812[i] == null) {
                return 1;
            }
            /* stream setup */
            name = sprintf("%s #%d", sound_name(msound), i);
            stream[i] = stream_init(name, vol, rate, i, YM3812UpdateHandler);
            /* YM3812 setup */
            OPLSetTimerHandler(F3812[i], TimerHandler, i * 2);
            OPLSetIRQHandler(F3812[i], IRQHandler, i);
            OPLSetUpdateHandler(F3812[i], stream_updateptr, stream[i]);
        }
        return 0;
    }

    public static int YM3812_sh_start(MachineSound msound) {
        chiptype = OPL_TYPE_YM3812;
        return OPL_sh_start(msound);
    }
    public static timer_callback timer_callback_3812 = new timer_callback() {
        public void handler(int param) {
            int n = param >> 1;
            int c = param & 1;
            Timer[param] = 0;
            OPLTimerOver(F3812[n], c);
        }
    };
    public static OPL_TIMERHANDLERPtr TimerHandler = new OPL_TIMERHANDLERPtr() {

        @Override
        public void handler(int c, double period) {
            if (period == 0) {
                /* Reset FM Timer */

                if (Timer[c] != null) {
                    timer_remove(Timer[c]);
                    Timer[c] = 0;
                }
            } else {
                /* Start FM Timer */

                Timer[c] = timer_set(period, c, timer_callback_3812);
            }
        }
    };
    public static OPL_IRQHANDLERPtr IRQHandler = new OPL_IRQHANDLERPtr() {

        @Override
        public void handler(int n, int irq) {
            if (intf.handler == null) {
                return;
            }
            if (intf.handler[n] != null) {
                (intf.handler[n]).handler(irq != 0 ? ASSERT_LINE : CLEAR_LINE);
            }
        }

    };
    public static OPL_UPDATEHANDLERPtr stream_updateptr = new OPL_UPDATEHANDLERPtr() {
        @Override
        public void handler(int param, int min_interval_us) {
            stream_update(param, min_interval_us);
        }
    };

    @Override
    public void stop() {
        YM3812_sh_stop();
    }

    public static void YM3812_sh_stop() {
        int i;

        for (i = 0; i < intf.num; i++) {
            OPLDestroy(F3812[i]);
        }
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }

    public static void YM3812_sh_reset() {
        int i;

        for (i = 0xff; i <= 0; i--) {
            YM3812_control_port_0_w.handler(0, i);
            YM3812_write_port_0_w.handler(0, 0);
        }
        /* IRQ clear */
        YM3812_control_port_0_w.handler(0, 4);
        YM3812_write_port_0_w.handler(0, 0x80);
    }
    public static ReadHandlerPtr YM3812_read_port_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return OPLRead(F3812[0], 1);
        }
    };
    public static ReadHandlerPtr YM3812_status_port_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return OPLRead(F3812[0], 0);
        }
    };
    public static WriteHandlerPtr YM3812_control_port_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            OPLWrite(F3812[0], 0, data);
        }
    };
    public static WriteHandlerPtr YM3812_write_port_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            OPLWrite(F3812[0], 1, data);
        }
    };
    public static ReadHandlerPtr YM3812_read_port_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return OPLRead(F3812[1], 1);
        }
    };
    public static ReadHandlerPtr YM3812_status_port_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return OPLRead(F3812[1], 0);
        }
    };
    public static WriteHandlerPtr YM3812_control_port_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            OPLWrite(F3812[1], 0, data);
        }
    };
    public static WriteHandlerPtr YM3812_write_port_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            OPLWrite(F3812[1], 1, data);
        }
    };
    public static StreamInitPtr YM3812UpdateHandler = new StreamInitPtr() {
        public void handler(int num, ShortPtr buffer, int length) {
            YM3812UpdateOne(F3812[num], buffer, length);
        }
    };
}
