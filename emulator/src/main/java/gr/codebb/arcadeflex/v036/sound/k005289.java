package gr.codebb.arcadeflex.v036.sound;

import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.k005289H.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.platform.libc_v2.*;
public class k005289 extends snd_interface {

    public k005289() {
        this.name = "005289";
        this.sound_num = SOUND_K005289;
    }

    @Override
    public int chips_num(MachineSound msound) {
        return 0;//no functionality expected
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return 0;//no functionality expected
    }

    public static class k005289_sound_channel {

        public int frequency, counter, volume;
        public UBytePtr wave;
    }
    public static final int FREQBASEBITS = 16;
    static k005289_sound_channel[] channel_list = new k005289_sound_channel[2];

    /* global sound parameters */
    static UBytePtr sound_prom;
    static int stream, mclock, rate;

    /* mixer tables and internal buffers */
    static /*INT16 */ UShortPtr mixer_table;
    static /*INT16 */ UShortPtr mixer_lookup;
    static /*short */ ShortPtr mixer_buffer;

    static int k005289_A_frequency, k005289_B_frequency;
    static int k005289_A_volume, k005289_B_volume;
    static int k005289_A_waveform, k005289_B_waveform;
    static int k005289_A_latch, k005289_B_latch;

    /* build a table to divide by the number of voices */
    static int make_mixer_table(int voices) {
        int count = voices * 128;
        int i;
        int gain = 16;

        /* allocate memory */
        mixer_table = new UShortPtr(256 * voices * /*sizeof(INT16)*/ 2);
        if (mixer_table == null) {
            return 1;
        }

        /* find the middle of the table */
        mixer_lookup = new UShortPtr(mixer_table, (128 * voices) * 2);//mixer_lookup = mixer_table + (128 * voices);

        /* fill in the table - 16 bit case */
        for (i = 0; i < count; i++) {
            int val = i * gain * 16 / voices;
            if (val > 32767) {
                val = 32767;
            }
            mixer_lookup.write(i, (char) val);
            mixer_lookup.write(-i, (char) -val);
        }

        return 0;
    }
    public static StreamInitPtr K005289_update = new StreamInitPtr() {
        public void handler(int chip, ShortPtr buffer, int length) {
            //k005289_sound_channel *voice=channel_list;
            ShortPtr mix;
            int i, v, f;

            /* zap the contents of the mixer buffer */
           // memset(mixer_buffer, 0, length * 2/*sizeof(INT16)*/);
            for (int k = 0; k < length * 2; k++) {
                buffer.memory[buffer.offset + k] = 0;
            }

            v = channel_list[0].volume;
            f = channel_list[0].frequency;
            if (v != 0 && f != 0) {
                UBytePtr w = channel_list[0].wave;
                int c = channel_list[0].counter;

                mix = new ShortPtr(mixer_buffer);

                /* add our contribution */
                for (i = 0; i < length; i++) {
                    int offs;

                    c += (long) ((((float) mclock / (float) (f * 16)) * (float) (1 << FREQBASEBITS)) / (float) (rate / 32));
                    offs = (c >> 16) & 0x1f;
                    //*mix++ += ((w[offs] & 0x0f) - 8) * v;
                    short _w = mix.read(0);
                    mix.write(0, (short) (_w + (short) (((w.read(offs) & 0x0f) - 8) * v)));
                    mix.offset += 2;
                }

                /* update the counter for this voice */
                channel_list[0].counter = c;
            }

            v = channel_list[1].volume;
            f = channel_list[1].frequency;
            if (v != 0 && f != 0) {
                UBytePtr w = channel_list[1].wave;
                int c = channel_list[1].counter;

                mix = new ShortPtr(mixer_buffer);

                /* add our contribution */
                for (i = 0; i < length; i++) {
                    int offs;

                    c += (long) ((((float) mclock / (float) (f * 16)) * (float) (1 << FREQBASEBITS)) / (float) (rate / 32));
                    offs = (c >> 16) & 0x1f;
                    //*mix++ += ((w[offs] & 0x0f) - 8) * v;
                    short _w = mix.read(0);
                    mix.write(0, (short) (_w + (short) (((w.read(offs) & 0x0f) - 8) * v)));
                    mix.offset += 2;
                }

                /* update the counter for this voice */
                channel_list[1].counter = c;
            }

            /* mix it down */
            mix = new ShortPtr(mixer_buffer);
            for (i = 0; i < length; i++) {
                //*buffer++ = mixer_lookup[*mix++];
                buffer.write(0, (short) mixer_lookup.read((short) mix.read(0)));
                buffer.offset += 2;
                mix.offset += 2;
            }
        }
    };

    @Override
    public int start(MachineSound msound) {
        String snd_name = "K005289";
        //k005289_sound_channel *voice=channel_list;
        k005289_interface intf = (k005289_interface) msound.sound_interface;

        /* get stream channels */
        stream = stream_init(snd_name, intf.volume, Machine.sample_rate, 0, K005289_update);
        mclock = intf.master_clock;
        rate = Machine.sample_rate;

        /* allocate a pair of buffers to mix into - 1 second's worth should be more than enough */
        mixer_buffer = new ShortPtr(2 * /*sizeof(short)*/ 2 * Machine.sample_rate);

        /* build the mixer table */
        if (make_mixer_table(2) != 0) {
            mixer_buffer = null;
            return 1;
        }

        sound_prom = memory_region(intf.region);

        /* reset all the voices */
        for(int i=0; i<2; i++)
        {
            channel_list[i]=new k005289_sound_channel();
        }
        channel_list[0].frequency = 0;
        channel_list[0].volume = 0;
        channel_list[0].wave = new UBytePtr(sound_prom);
        channel_list[0].counter = 0;
        channel_list[1].frequency = 0;
        channel_list[1].volume = 0;
        channel_list[1].wave = new UBytePtr(sound_prom, 0x100);
        channel_list[1].counter = 0;

        return 0;
    }

    @Override
    public void stop() {
        mixer_table = null;
        mixer_buffer = null;
    }

    static void k005289_recompute() {
        //k005289_sound_channel *voice = channel_list;

        stream_update(stream, 0); 	/* update the streams */

        channel_list[0].frequency = k005289_A_frequency;
        channel_list[1].frequency = k005289_B_frequency;
        channel_list[0].volume = k005289_A_volume;
        channel_list[1].volume = k005289_B_volume;
        channel_list[0].wave = new UBytePtr(sound_prom, 32 * k005289_A_waveform);
        channel_list[1].wave = new UBytePtr(sound_prom, 32 * k005289_B_waveform + 0x100);
    }
    public static WriteHandlerPtr k005289_control_A_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            k005289_A_volume = data & 0xf;
            k005289_A_waveform = data >> 5;
            k005289_recompute();
        }
    };

    public static WriteHandlerPtr k005289_control_B_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            k005289_B_volume = data & 0xf;
            k005289_B_waveform = data >> 5;
            k005289_recompute();
        }
    };

    public static WriteHandlerPtr k005289_pitch_A_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            k005289_A_latch = 0x1000 - offset;
        }
    };

    public static WriteHandlerPtr k005289_pitch_B_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            k005289_B_latch = 0x1000 - offset;
        }
    };

    public static WriteHandlerPtr k005289_keylatch_A_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            k005289_A_frequency = k005289_A_latch;
            k005289_recompute();
        }
    };

    public static WriteHandlerPtr k005289_keylatch_B_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            k005289_B_frequency = k005289_B_latch;
            k005289_recompute();
        }
    };

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }

}
