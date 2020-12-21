package gr.codebb.arcadeflex.v036.sound;

import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound._2151intfH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.v036.sound.ym2151.*;


public class _2151intf extends snd_interface {

    public _2151intf() {
        this.name = "YM-2151";
        this.sound_num = SOUND_YM2151;
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

    public static void YM2151UpdateRequest(int chip) {
        stream_update(stream[chip], 0);
    }

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
            /* stream setup */
            mixed_vol = intf.volume[i];
            for (j = 0; j < YM2151_NUMBUF; j++) {
                //name[j]=buf[j];
                vol[j] = mixed_vol & 0xffff;
                mixed_vol >>= 16;
                name[j] = sprintf("%s #%d Ch%d", sound_name(msound), i, j + 1);//sprintf(buf[j],"%s #%d Ch%d",sound_name(msound),i,j+1);
            }
            stream[i] = stream_init_multi(YM2151_NUMBUF, name, vol, rate, i, YM2151UpdateOne);
        }
        if (YM2151Init(intf.num, intf.baseclock, Machine.sample_rate) == 0) {
            for (i = 0; i < intf.num; i++) {
                YM2151SetIrqHandler(i, intf.irqhandler[i]);
                YM2151SetPortWriteHandler(i, intf.portwritehandler[i]);
            }
            return 0;
        }
        return 1;
    }

    @Override
    public int start(MachineSound msound) {
        return my_YM2151_sh_start(msound, 1);
    }

    @Override
    public void stop() {
        YM2151Shutdown();
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        int i;

        for (i = 0; i < intf.num; i++) {
            YM2151ResetChip(i);
        }
    }

    static int lastreg0, lastreg1, lastreg2;

    public static ReadHandlerPtr YM2151_status_port_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2151ReadStatus(0);
        }
    };

    public static ReadHandlerPtr YM2151_status_port_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2151ReadStatus(1);
        }
    };

    public static ReadHandlerPtr YM2151_status_port_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2151ReadStatus(2);
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
            YM2151UpdateRequest(0);
            YM2151WriteReg(0, lastreg0, data);
        }
    };

    public static WriteHandlerPtr YM2151_data_port_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2151UpdateRequest(1);
            YM2151WriteReg(1, lastreg1, data);
        }
    };

    public static WriteHandlerPtr YM2151_data_port_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2151UpdateRequest(2);
            YM2151WriteReg(2, lastreg2, data);
        }
    };

}
