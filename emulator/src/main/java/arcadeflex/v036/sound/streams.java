/**
 *  ported to 0.36
 */
package arcadeflex.v036.sound;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.sound.mixer.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.MIXER_MAX_CHANNELS;

public class streams {

    public static abstract interface StreamInitPtr {

        public abstract void handler(int param, ShortPtr buffer, int length);
    }

    public static abstract interface StreamInitMultiPtr {

        public abstract void handler(int param, ShortPtr[] buffer, int length);
    }

    public static final int BUFFER_LEN = 16384;

    public static int SAMPLES_THIS_FRAME(int channel) {
        return mixer_need_samples_this_frame((channel), stream_sample_rate[(channel)]);
    }

    static int[] stream_joined_channels = new int[MIXER_MAX_CHANNELS];
    static ShortPtr[] stream_buffer = new ShortPtr[MIXER_MAX_CHANNELS];//static INT16 *stream_buffer[MIXER_MAX_CHANNELS];

    static int[] stream_sample_rate = new int[MIXER_MAX_CHANNELS];
    static int[] stream_buffer_pos = new int[MIXER_MAX_CHANNELS];
    static int[] stream_sample_length = new int[MIXER_MAX_CHANNELS];
    /* in usec */

    static int[] stream_param = new int[MIXER_MAX_CHANNELS];
    static StreamInitPtr[] stream_callback = new StreamInitPtr[MIXER_MAX_CHANNELS];
    static StreamInitMultiPtr[] stream_callback_multi = new StreamInitMultiPtr[MIXER_MAX_CHANNELS];

    static int[] memory = new int[MIXER_MAX_CHANNELS];
    static int[] r1 = new int[MIXER_MAX_CHANNELS];
    static int[] r2 = new int[MIXER_MAX_CHANNELS];
    static int[] r3 = new int[MIXER_MAX_CHANNELS];
    static int[] c = new int[MIXER_MAX_CHANNELS];

    /*
     signal >--R1--+--R2--+
     |      |
     C      R3---> amp
     |      |
     GND    GND
     */
 /* R1, R2, R3 in Ohm; C in pF */
 /* set C = 0 to disable the filter */
    public static void set_RC_filter(int channel, int R1, int R2, int R3, int C) {
        r1[channel] = R1;
        r2[channel] = R2;
        r3[channel] = R3;
        c[channel] = C;
    }

    public static void apply_RC_filter(int channel, ShortPtr buf, int len, int sample_rate) {
        if (c[channel] == 0) {
            return;
            /* filter disabled */

        }

        float R1 = r1[channel];
        float R2 = r2[channel];
        float R3 = r3[channel];
        float C = (float) (c[channel] * 1E-12);
        /* convert pF to F */

 /* Cut Frequency = 1/(2*Pi*Req*C) */

        float Req = (R1 * (R2 + R3)) / (R1 + R2 + R3);

        int K = (int) (0x10000 * Math.exp(-1 / (Req * C) / sample_rate));

        buf.write(0, (short) (buf.read(0) + (memory[channel] - buf.read(0)) * K / 0x10000));

        for (int i = 1; i < len; i++) {
            buf.write(i, (short) (buf.read(i) + (buf.read(i - 1) - buf.read(i)) * K / 0x10000));
        }

        memory[channel] = buf.read(len - 1);
    }

    public static int streams_sh_start() {
        int i;

        for (i = 0; i < MIXER_MAX_CHANNELS; i++) {
            stream_joined_channels[i] = 1;
            stream_buffer[i] = null;
        }

        return 0;
    }

    public static void streams_sh_stop() {
        int i;

        for (i = 0; i < MIXER_MAX_CHANNELS; i++) {
            stream_buffer[i] = null;
        }
    }

    public static void streams_sh_update() {
        int channel, i;

        if (Machine.sample_rate == 0) {
            return;
        }

        /* update all the output buffers */
        for (channel = 0; channel < MIXER_MAX_CHANNELS; channel += stream_joined_channels[channel]) {
            if (stream_buffer[channel] != null) {
                int newpos;
                int buflen;

                newpos = SAMPLES_THIS_FRAME(channel);

                buflen = newpos - stream_buffer_pos[channel];

                if (stream_joined_channels[channel] > 1) {

                    ShortPtr[] buf = new ShortPtr[MIXER_MAX_CHANNELS];

                    if (buflen > 0) {
                        for (i = 0; i < stream_joined_channels[channel]; i++) {
                            buf[i] = new ShortPtr(stream_buffer[channel + i], stream_buffer_pos[channel + i] * 2);
                        }

                        stream_callback_multi[channel].handler(stream_param[channel], buf, buflen);
                    }

                    for (i = 0; i < stream_joined_channels[channel]; i++) {
                        stream_buffer_pos[channel + i] = 0;
                    }

                    for (i = 0; i < stream_joined_channels[channel]; i++) {
                        apply_RC_filter(channel + i, stream_buffer[channel + i], buflen, stream_sample_rate[channel + i]);
                    }

                } else {
                    if (buflen > 0) {
                        ShortPtr buf = new ShortPtr(stream_buffer[channel], stream_buffer_pos[channel] * 2);//INT16 *buf= stream_buffer[channel] + stream_buffer_pos[channel];
                        stream_callback[channel].handler(stream_param[channel], buf, buflen);

                    }

                    stream_buffer_pos[channel] = 0;

                    apply_RC_filter(channel, stream_buffer[channel], buflen, stream_sample_rate[channel]);
                }
            }
        }

        for (channel = 0; channel < MIXER_MAX_CHANNELS; channel += stream_joined_channels[channel]) {
            if (stream_buffer[channel] != null) {
                for (i = 0; i < stream_joined_channels[channel]; i++) {
                    mixer_play_streamed_sample_16(channel + i,
                            stream_buffer[channel + i], 2 * SAMPLES_THIS_FRAME(channel + i),
                            stream_sample_rate[channel]);
                }
            }
        }
    }

    public static int stream_init(String name, int default_mixing_level,
            int sample_rate,
            int param, StreamInitPtr callback) {
        int channel;

        channel = mixer_allocate_channel(default_mixing_level);

        stream_joined_channels[channel] = 1;

        mixer_set_name(channel, name);

        stream_buffer[channel] = new ShortPtr(2 * BUFFER_LEN);//if ((stream_buffer[channel] = malloc(sizeof(INT16)*BUFFER_LEN)) == 0) return -1;

        stream_sample_rate[channel] = sample_rate;
        stream_buffer_pos[channel] = 0;
        if (sample_rate != 0) {
            stream_sample_length[channel] = 1000000 / sample_rate;
        } else {
            stream_sample_length[channel] = 0;
        }
        stream_param[channel] = param;
        stream_callback[channel] = callback;
        set_RC_filter(channel, 0, 0, 0, 0);

        return channel;
    }

    public static int stream_init_multi(int channels, String[] names, int[] default_mixing_levels,
            int sample_rate,
            int param, StreamInitMultiPtr callback) {
        int channel, i;

        channel = mixer_allocate_channels(channels, default_mixing_levels);

        stream_joined_channels[channel] = channels;

        for (i = 0; i < channels; i++) {
            mixer_set_name(channel + i, names[i]);

            stream_buffer[channel + i] = new ShortPtr(2 * BUFFER_LEN);// if ((stream_buffer[channel + i] = malloc(sizeof(INT16) * BUFFER_LEN)) == 0)

            stream_sample_rate[channel + i] = sample_rate;
            stream_buffer_pos[channel + i] = 0;
            if (sample_rate != 0) {
                stream_sample_length[channel + i] = 1000000 / sample_rate;
            } else {
                stream_sample_length[channel + i] = 0;
            }
        }

        stream_param[channel] = param;
        stream_callback_multi[channel] = callback;
        set_RC_filter(channel, 0, 0, 0, 0);

        return channel;
    }


    /* min_interval is in usec */
    public static void stream_update(int channel, int min_interval) {
        int newpos;
        int buflen;

        if (Machine.sample_rate == 0 || stream_buffer[channel] == null) {
            return;
        }

        /* get current position based on the timer */
        newpos = sound_scalebufferpos(SAMPLES_THIS_FRAME(channel));

        buflen = newpos - stream_buffer_pos[channel];

        if (buflen * stream_sample_length[channel] > min_interval) {
            if (stream_joined_channels[channel] > 1) {
                ShortPtr[] buf = new ShortPtr[MIXER_MAX_CHANNELS];

                for (int i = 0; i < stream_joined_channels[channel]; i++) {
                    buf[i] = new ShortPtr(stream_buffer[channel + i], stream_buffer_pos[channel + i] * 2);
                }

                stream_callback_multi[channel].handler(stream_param[channel], buf, buflen);

                for (int i = 0; i < stream_joined_channels[channel]; i++) {
                    stream_buffer_pos[channel + i] += buflen;
                }
            } else {
                ShortPtr buf = new ShortPtr(stream_buffer[channel], stream_buffer_pos[channel] * 2);//INT16 *buf = stream_buffer[channel] + stream_buffer_pos[channel];

                stream_callback[channel].handler(stream_param[channel], buf, buflen);

                stream_buffer_pos[channel] += buflen;
            }
        }
    }
}
