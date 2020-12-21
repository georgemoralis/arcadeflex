/*
 *  this file should be fully compatible for 0.36
 */
package gr.codebb.arcadeflex.v036.sound;

import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.dacH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.platform.libc_v2.*;
public class dac extends snd_interface {

    static int[] channel = new int[MAX_DAC];
    static int[] output = new int[MAX_DAC];
    static int[] UnsignedVolTable = new int[256];
    static int[] SignedVolTable = new int[256];

    public dac() {
        sound_num = SOUND_DAC;
        name = "DAC";
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((DACinterface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return 0;//NO FUNCTIONAL CODE IS NECCESARY
    }

    @Override
    public void stop() {
        //NO FUNCTIONAL CODE IS NECCESARY
    }

    @Override
    public void update() {
        //NO FUNCTIONAL CODE IS NECCESARY
    }

    @Override
    public void reset() {
        //NO FUNCTIONAL CODE IS NECCESARY
    }

    public static StreamInitPtr DAC_update = new StreamInitPtr() {
        public void handler(int num, ShortPtr buffer, int length) {
            int out = output[num];
            int bi = 0;
            while (length-- != 0) {
                buffer.write(bi++, (short) out);//while (length--) *(buffer++) = out;
            }
        }
    };

    public static WriteHandlerPtr DAC_data_w = new WriteHandlerPtr() {
        public void handler(int num, int data) {
            int out = UnsignedVolTable[data];

            if (output[num] != out) {
                /* update the output buffer before changing the registers */
                stream_update(channel[num], 0);
                output[num] = out;
            }
        }
    };

    public static WriteHandlerPtr DAC_signed_data_w = new WriteHandlerPtr() {
        public void handler(int num, int data) {
            int out = SignedVolTable[data];

            if (output[num] != out) {
                /* update the output buffer before changing the registers */
                stream_update(channel[num], 0);
                output[num] = out;
            }
        }
    };

    public static WriteHandlerPtr DAC_data_16_w = new WriteHandlerPtr() {
        public void handler(int num, int data) {
            int out = data >> 1;		/* range      0..32767 */

            if (output[num] != out) {
                /* update the output buffer before changing the registers */
                stream_update(channel[num], 0);
                output[num] = out;
            }
        }
    };

    public static WriteHandlerPtr DAC_signed_data_16_w = new WriteHandlerPtr() {
        public void handler(int num, int data) {
            int out = data - 0x8000;	/* range -32768..32767 */

            if (output[num] != out) {
                /* update the output buffer before changing the registers */
                stream_update(channel[num], 0);
                output[num] = out;
            }
        }
    };

    static void DAC_build_voltable() {
        int i;

        /* build volume table (linear) */
        for (i = 0; i < 256; i++) {
            UnsignedVolTable[i] = i * 0x101 / 2;	/* range      0..32767 */

            SignedVolTable[i] = i * 0x101 - 0x8000;	/* range -32768..32767 */

        }
    }

    @Override
    public int start(MachineSound msound) {
        int i;

        DACinterface intf = ((DACinterface) msound.sound_interface);

        DAC_build_voltable();

        for (i = 0; i < intf.num; i++) {
            String name;

            name = sprintf("DAC #%d", i);
            channel[i] = stream_init(name, intf.mixing_level[i], Machine.sample_rate, i, DAC_update);

            if (channel[i] == -1) {
                return 1;
            }

            output[i] = 0;
        }

        return 0;
    }

}
