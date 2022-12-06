/**
 * ported to 0.36
 */
package arcadeflex.v036.sndhrdw;

//cpu imports
import static arcadeflex.v036.cpu.s2650.s2650.*;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.BytePtr;
import static gr.codebb.arcadeflex.v036.mame.mame.errorlog;
import arcadeflex.v036.mame.sndintrfH.MachineSound;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fprintf;
import static gr.codebb.arcadeflex.v036.sound.mixer.mixer_allocate_channels;
import static gr.codebb.arcadeflex.v036.sound.mixer.mixer_play_sample;
import static gr.codebb.arcadeflex.v036.sound.mixer.mixer_set_sample_frequency;
import static gr.codebb.arcadeflex.v036.sound.mixer.mixer_set_volume;
import static gr.codebb.arcadeflex.v036.sound.mixer.mixer_stop_sample;
import static gr.codebb.arcadeflex.v037b7.sound.dac.DAC_data_w;

public class meadows {

    public static /*unsigned*/ char meadows_0c00 = 0;
    public static /*unsigned*/ char meadows_0c01 = 0;
    public static /*unsigned*/ char meadows_0c02 = 0;
    public static /*unsigned*/ char meadows_0c03 = 0;
    public static /*unsigned*/ char meadows_dac = 0;
    static int dac_enable;

    public static final int BASE_CLOCK = 5000000;
    public static final int BASE_CTR1 = (BASE_CLOCK / 256);
    public static final int BASE_CTR2 = (BASE_CLOCK / 32);

    public static final int DIV2OR4_CTR2 = 0x01;
    public static final int ENABLE_CTR2 = 0x02;
    public static final int ENABLE_DAC = 0x04;
    public static final int ENABLE_CTR1 = 0x08;

    static int channel;
    static int freq1 = 1000;
    static int freq2 = 1000;
    static byte waveform[] = {-120, 120};

    /**
     * *********************************
     */
    /* Sound handler start				*/
    /**
     * *********************************
     */
    public static ShStartHandlerPtr meadows_sh_start = new ShStartHandlerPtr() {
        public int handler(MachineSound msound) {
            int[] vol = new int[2];

            vol[0] = vol[1] = 255;
            channel = mixer_allocate_channels(2, vol);
            mixer_set_volume(channel, 0);
            mixer_play_sample(channel, new BytePtr(waveform), waveform.length, freq1, 1);
            mixer_set_volume(channel + 1, 0);
            mixer_play_sample(channel + 1, new BytePtr(waveform), waveform.length, freq2, 1);
            return 0;
        }
    };

    /**
     * *********************************
     */
    /* Sound handler stop				*/
    /**
     * *********************************
     */
    public static ShStopHandlerPtr meadows_sh_stop = new ShStopHandlerPtr() {
        public void handler() {
            mixer_stop_sample(channel);
            mixer_stop_sample(channel + 1);
        }
    };

    /**
     * *********************************
     */
    /* Sound handler update 			*/
    /**
     * *********************************
     */
    public static /*unsigned*/ char latched_0c01 = 0;
    public static /*unsigned*/ char latched_0c02 = 0;
    public static /*unsigned*/ char latched_0c03 = 0;
    public static ShUpdateHandlerPtr meadows_sh_update = new ShUpdateHandlerPtr() {
        public void handler() {

            int preset, amp;

            if (latched_0c01 != meadows_0c01 || latched_0c03 != meadows_0c03) {
                /* amplitude is a combination of the upper 4 bits of 0c01 */
 /* and bit 4 merged from S2650's flag output */
                amp = ((meadows_0c03 & ENABLE_CTR1) == 0) ? 0 : (meadows_0c01 & 0xf0) >> 1;
                if (s2650_get_flag() != 0) {
                    amp += 0x80;
                }
                /* calculate frequency for counter #1 */
 /* bit 0..3 of 0c01 are ctr preset */
                preset = (meadows_0c01 & 15) ^ 15;
                if (preset != 0) {
                    freq1 = BASE_CTR1 / (preset + 1);
                } else {
                    amp = 0;
                }
                if (errorlog != null) {
                    fprintf(errorlog, "meadows ctr1 channel #%d preset:%3d freq:%5d amp:%d\n", channel, preset, freq1, amp);
                }
                mixer_set_sample_frequency(channel, freq1 * waveform.length);
                mixer_set_volume(channel, amp * 100 / 255);
            }

            if (latched_0c02 != meadows_0c02 || latched_0c03 != meadows_0c03) {
                /* calculate frequency for counter #2 */
 /* 0c02 is ctr preset, 0c03 bit 0 enables division by 2 */
                amp = ((meadows_0c03 & ENABLE_CTR2) != 0) ? 0xa0 : 0;
                preset = meadows_0c02 ^ 0xff;
                if (preset != 0) {
                    freq2 = BASE_CTR2 / (preset + 1) / 2;
                    if ((meadows_0c03 & DIV2OR4_CTR2) == 0) {
                        freq2 >>= 1;
                    }
                } else {
                    amp = 0;
                }
                if (errorlog != null) {
                    fprintf(errorlog, "meadows ctr2 channel #%d preset:%3d freq:%5d amp:%d\n", channel + 1, preset, freq2, amp);
                }
                mixer_set_sample_frequency(channel + 1, freq2 * waveform.length);
                mixer_set_volume(channel + 1, amp * 100 / 255);
            }

            if (latched_0c03 != meadows_0c03) {
                dac_enable = meadows_0c03 & ENABLE_DAC;

                if (dac_enable != 0) {
                    DAC_data_w.handler(0, meadows_dac);
                } else {
                    DAC_data_w.handler(0, 0);
                }
            }

            latched_0c01 = meadows_0c01;
            latched_0c02 = meadows_0c02;
            latched_0c03 = meadows_0c03;
        }
    };

    /**
     * *********************************
     */
    /* Write DAC value					*/
    /**
     * *********************************
     */
    public static void meadows_sh_dac_w(int data) {
        meadows_dac = (char) (data & 0xFF);
        if (dac_enable != 0) {
            DAC_data_w.handler(0, meadows_dac);
        } else {
            DAC_data_w.handler(0, 0);
        }
    }
}
