/*
 * ported to v0.36
 * 
 */
package arcadeflex.v036.sound;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.sound.MSM5205H.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.sound.streams.*;
import arcadeflex.v036.mame.sndintrf.snd_interface;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;

public class MSM5205 extends snd_interface {

    /*
     * ADPCM lockup tabe
     */

 /* step size index shift table */
    static int index_shift[] = {-1, -1, -1, -1, 2, 4, 6, 8};

    /* lookup table for the precomputed difference */
    static int[] diff_lookup = new int[49 * 16];

    public static void ComputeTables() {
        /* nibble to bit map */
        int[][] nbl2bit
                = {
                    new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 1}, new int[]{1, 0, 1, 0}, new int[]{1, 0, 1, 1},
                    new int[]{1, 1, 0, 0}, new int[]{1, 1, 0, 1}, new int[]{1, 1, 1, 0}, new int[]{1, 1, 1, 1},
                    new int[]{-1, 0, 0, 0}, new int[]{-1, 0, 0, 1}, new int[]{-1, 0, 1, 0}, new int[]{-1, 0, 1, 1},
                    new int[]{-1, 1, 0, 0}, new int[]{-1, 1, 0, 1}, new int[]{-1, 1, 1, 0}, new int[]{-1, 1, 1, 1}
                };

        int step, nib;

        /* loop over all possible steps */
        for (step = 0; step <= 48; step++) {
            /* compute the step value */
            int stepval = (int) (Math.floor(16.0 * Math.pow(11.0 / 10.0, (double) step)));


            /* loop over all nibbles and compute the difference */
            for (nib = 0; nib < 16; nib++) {
                diff_lookup[step * 16 + nib] = nbl2bit[nib][0]
                        * (stepval * nbl2bit[nib][1]
                        + stepval / 2 * nbl2bit[nib][2]
                        + stepval / 4 * nbl2bit[nib][3]
                        + stepval / 8);
            }
        }
    }

    /*
     *
     *	MSM 5205 ADPCM chip:
     *
     *	Data is streamed from a CPU by means of a clock generated on the chip.
     *
     *	A reset signal is set high or low to determine whether playback (and interrupts) are occuring
     *
     */

    public static class MSM5205Voice {

        int stream;
        /* number of stream system      */

        Object timer;
        /* VCLK callback timer          */

        int data;
        /* next adpcm data              */

        int vclk;
        /* vclk signal (external mode)  */

        int reset;
        /* reset pin signal             */

        int prescaler;
        /* prescaler selector S1 and S2 */

        int bitwidth;
        /* bit width selector -3B/4B    */

        int signal;
        /* current ADPCM signal         */

        int step;
        /* current ADPCM step           */

    };

    static MSM5205interface msm5205_intf;
    static MSM5205Voice[] msm5205 = new MSM5205Voice[MAX_MSM5205];

    public MSM5205() {
        this.sound_num = SOUND_MSM5205;
        this.name = "MSM5205";
        for (int i = 0; i < MAX_MSM5205; i++) {
            msm5205[i] = new MSM5205Voice();
        }
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((MSM5205interface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((MSM5205interface) msound.sound_interface).baseclock;
    }

    @Override
    public int start(MachineSound msound) {
        int i;

        /* save a global pointer to our interface */
        msm5205_intf = (MSM5205interface) msound.sound_interface;

        /* compute the difference tables */
        ComputeTables();

        /* initialize the voices */
        //memset (msm5205, 0, sizeof (msm5205));

        /* stream system initialize */
        for (i = 0; i < msm5205_intf.num; i++) {
            MSM5205Voice voice = msm5205[i];
            String name = sprintf("MSM5205 #%d", i);
            voice.stream = stream_init(name, msm5205_intf.mixing_level[i], Machine.sample_rate, i, MSM5205_update);
        }
        /* initialize */
        MSM5205_sh_reset();
        /* success */
        return 0;
    }

    @Override
    public void stop() {
        //NO FUNCTIONALITY EXPECTED
    }

    @Override
    public void update() {
        //NO FUNCTIONALITY EXPECTED
    }

    @Override
    public void reset() {
        MSM5205_sh_reset();
    }

    /*
     *    Reset emulation of an MSM5205-compatible chip
     */

    public static void MSM5205_sh_reset() {
        int i;

        /* bail if we're not emulating sound */
        if (Machine.sample_rate == 0) {
            return;
        }

        for (i = 0; i < msm5205_intf.num; i++) {
            MSM5205Voice voice = msm5205[i];
            /* initialize work */
            voice.data = 0;
            voice.vclk = 0;
            voice.reset = 0;
            voice.signal = 0;
            voice.step = 0;
            /* timer set */
            MSM5205_set_timer(i, msm5205_intf.select[i] & 0x03);
            /* bitwidth reset */
            msm5205[i].bitwidth = (msm5205_intf.select[i] & 0x04) != 0 ? 4 : 3;
        }
    }

    static void MSM5205_set_timer(int num, int select) {
        MSM5205Voice voice = msm5205[num];
        int prescaler_table[] = {96, 48, 64, 0};
        int prescaler = prescaler_table[select & 0x03];

        if (voice.prescaler != prescaler) {
            /* remove VCLK timer */
            if (voice.timer != null) {
                timer_remove(voice.timer);
                voice.timer = null;
            }
            voice.prescaler = prescaler;
            /* timer set */
            if (prescaler != 0) {
                voice.timer = timer_pulse(TIME_IN_HZ(msm5205_intf.baseclock / prescaler), num, MSM5205_vclk_callback);
            }
        }
    }
    public static StreamInitPtr MSM5205_update = new StreamInitPtr() {
        public void handler(int chip, ShortPtr buffer, int length) {
            MSM5205Voice voice = msm5205[chip];

            /* if this voice is active */
            if (voice.signal != 0) {
                short val = (short) (voice.signal * 16);
                int i = 0;
                while (length != 0) {
                    buffer.write(i++, (short) val);
                    length--;
                }
            } else {
                for (int i = 0; i < buffer.memory.length; i++)//memset (buffer,0,length*sizeof(INT16));
                {
                    buffer.memory[i] = 0;
                }
            }
        }
    };
    public static TimerCallbackHandlerPtr MSM5205_vclk_callback = new TimerCallbackHandlerPtr() {
        public void handler(int num) {
            MSM5205Voice voice = msm5205[num];
            int val;
            int new_signal;
            /* callback user handler and latch next data */
            if (msm5205_intf.vclk_interrupt[num] != null) {
                msm5205_intf.vclk_interrupt[num].handler(num);
            }

            /* reset check at last hieddge of VCLK */
            if (voice.reset != 0) {
                new_signal = 0;
                voice.step = 0;
            } else {
                /* update signal */
 /* !! MSM5205 has internal 12bit decoding, signal width is 0 to 8191 !! */
                val = voice.data;
                new_signal = voice.signal + diff_lookup[voice.step * 16 + (val & 15)];
                if (new_signal > 2047) {
                    new_signal = 2047;
                } else if (new_signal < -2048) {
                    new_signal = -2048;
                }
                voice.step += index_shift[val & 7];
                if (voice.step > 48) {
                    voice.step = 48;
                } else if (voice.step < 0) {
                    voice.step = 0;
                }
            }
            /* update when signal changed */
            if (voice.signal != new_signal) {
                stream_update(voice.stream, 0);
                voice.signal = new_signal;
            }
        }
    };
    /*
     *    Handle an update of the vclk status of a chip (1 is reset ON, 0 is reset OFF)
     *    This function can use selector = MSM5205_SEX only
     */
    public static WriteHandlerPtr MSM5205_vclk_w = new WriteHandlerPtr() {
        public void handler(int num, int vclk) {
            /* range check the numbers */
            if (num >= msm5205_intf.num) {
                if (errorlog != null) {
                    fprintf(errorlog, "error: MSM5205_vclk_w() called with chip = %d, but only %d chips allocated\n", num, msm5205_intf.num);
                }
                return;
            }
            if (msm5205[num].prescaler != 0) {
                if (errorlog != null) {
                    fprintf(errorlog, "error: MSM5205_vclk_w() called with chip = %d, but VCLK selected master mode\n", num);
                }
            } else {
                if (msm5205[num].vclk != vclk) {
                    msm5205[num].vclk = vclk;
                    if (vclk == 0) {
                        MSM5205_vclk_callback.handler(num);
                    }
                }
            }
        }
    };

    /*
     *    Handle an update of the reset status of a chip (1 is reset ON, 0 is reset OFF)
     */
    public static WriteHandlerPtr MSM5205_reset_w = new WriteHandlerPtr() {
        public void handler(int num, int reset) {
            /* range check the numbers */
            if (num >= msm5205_intf.num) {
                if (errorlog != null) {
                    fprintf(errorlog, "error: MSM5205_reset_w() called with chip = %d, but only %d chips allocated\n", num, msm5205_intf.num);
                }
                return;
            }
            msm5205[num].reset = reset;
        }
    };

    /*
     *    Handle an update of the data to the chip
     */
    public static WriteHandlerPtr MSM5205_data_w = new WriteHandlerPtr() {
        public void handler(int num, int data) {
            if (msm5205[num].bitwidth == 4) {
                msm5205[num].data = data & 0x0f;
            } else {
                msm5205[num].data = (data & 0x07) << 1;
                /* unknown */

            }
        }
    };

    /*
     *    Handle an change of the selector
     */
    public static WriteHandlerPtr MSM5205_selector_w = new WriteHandlerPtr() {
        public void handler(int num, int select) {

            MSM5205Voice voice = msm5205[num];

            stream_update(voice.stream, 0);
            MSM5205_set_timer(num, select);
        }
    };
    /* bitsel = -3B/4B pin : 0= 3bit , 1=4bit */
    public static WriteHandlerPtr MSM5205_bitwidth_w = new WriteHandlerPtr() {
        public void handler(int num, int bitsel) {
            int bitwidth = bitsel != 0 ? 4 : 3;
            if (msm5205[num].bitwidth != bitwidth) {
                stream_update(msm5205[num].stream, 0);
                msm5205[num].bitwidth = bitwidth;
            }
        }
    };
}
