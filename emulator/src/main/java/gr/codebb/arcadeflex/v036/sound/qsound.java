package gr.codebb.arcadeflex.v036.sound;

import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.qsoundH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.mame.timerH.*;

public class qsound extends snd_interface {

    public qsound() {
        this.sound_num = SOUND_QSOUND;
        this.name = "QSound";
    }

    @Override
    public int chips_num(MachineSound msound) {
        return 0;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((QSound_interface) msound.sound_interface).clock;
    }

    @Override
    public int start(MachineSound msound) {
        return 0;
    }

    @Override
    public void stop() {
        if (Machine.sample_rate == 0) {
            return;
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
    public static WriteHandlerPtr qsound_data_h_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
    //qsound_data=(qsound_data&0xff)|(data<<8);
        }
    };
    public static WriteHandlerPtr qsound_data_l_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
    //qsound_data=(qsound_data&0xff00)|data;
        }
    };
    public static WriteHandlerPtr qsound_cmd_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
    //qsound_set_command(data, qsound_data);
        }
    };
    public static ReadHandlerPtr qsound_status_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* Port ready bit (0x80 if ready) */
            return 0x80;
        }
    };

}
