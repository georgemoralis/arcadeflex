/**
 *  ported to 0.36
 */
package arcadeflex.v036.sndhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//common imports
import static common.libc.cstring.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.ShortPtr;
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import arcadeflex.v036.mame.sndintrfH.MachineSound;
import static arcadeflex.v036.sound.streams.*;

public class wiping {

    /* 8 voices max */
    public static final int MAX_VOICES = 8;

    static int samplerate = 48000;
    static int defgain = 48;

    /* this structure defines the parameters for a channel */
    public static class sound_channel {

        int frequency;
        int counter;
        int[] volume = new int[2];
        UBytePtr wave;
        int oneshot;
        int oneshotplaying;
    }

    /* globals available to everyone */
    public static UBytePtr wiping_soundregs = new UBytePtr();
    public static UBytePtr wiping_wavedata = new UBytePtr();

    /* data about the sound system */
    static sound_channel[] channel_list = new sound_channel[MAX_VOICES];
    static int last_channel;

    /* global sound parameters */
    static UBytePtr sound_prom;
    static UBytePtr sound_rom;
    static int num_voices;
    static int sound_enable;
    static int stream;

    /* mixer tables and internal buffers */
    //static INT16 *mixer_table;
    static short[] mixer_lookup;
    static ShortPtr mixer_buffer;
    static ShortPtr mixer_buffer_2;

    /* build a table to divide by the number of voices; gain is specified as gain*16 */
    static int mixer_lookup_middle;

    static int make_mixer_table(int voices, int gain) {
        int count = voices * 128;
        int i;

        /* allocate memory */
        //mixer_table = malloc(256 * voices * sizeof(INT16));
        //if (!mixer_table)
        //	return 1;
        /* find the middle of the table */
        //mixer_lookup = mixer_table + (128 * voices);
        mixer_lookup = new short[256 * voices];
        mixer_lookup_middle = voices * 128;
        /* fill in the table - 16 bit case */
        for (i = 0; i < count; i++) {
            short val = (short) (i * gain * 16 / voices);
            if (val > 32767) {
                val = 32767;
            }
            mixer_lookup[mixer_lookup_middle + i] = val;
            mixer_lookup[mixer_lookup_middle - i] = (short) -val;
        }

        return 0;
    }

    /* generate sound to the mix buffer in mono */
    public static StreamInitPtr wiping_update_mono = new StreamInitPtr() {
        public void handler(int ch, ShortPtr buffer, int length) {
            ShortPtr mix;

            /* if no sound, we're done */
            if (sound_enable == 0) {
                memset(buffer, 0, length * 2);
                return;
            }

            /* zap the contents of the mixer buffer */
            //memset(mixer_buffer, 0, length * sizeof(short));
            for (int i = 0; i < length * 2; i++) {
                mixer_buffer.memory[mixer_buffer.offset + i] = 0;
            }

            /* loop over each voice and add its contribution */
            for (int voice = 0; voice < last_channel; voice++)//; voice < last_channel; voice++)
            {
                int f = 16 * channel_list[voice].frequency;
                int v = channel_list[voice].volume[0];

                /* only update if we have non-zero volume and frequency */
                if (v != 0 && f != 0) {
                    UBytePtr w = new UBytePtr(channel_list[voice].wave);
                    int c = channel_list[voice].counter;

                    mix = new ShortPtr(mixer_buffer);

                    /* add our contribution */
                    for (int i = 0; i < length; i++) {
                        int offs;

                        c += f;

                        if (channel_list[voice].oneshot != 0) {
                            if (channel_list[voice].oneshotplaying != 0) {
                                offs = (c >>> 15);
                                if (w.read(offs >>> 1) == 0xff) {
                                    channel_list[voice].oneshotplaying = 0;
                                }

                                if (channel_list[voice].oneshotplaying != 0) {
                                    /* use full byte, first the high 4 bits, then the low 4 bits */
                                    if ((offs & 1) != 0) {
                                        mix.write(0, (short) (mix.read(0) + ((w.read(offs >> 1) & 0x0f) - 8) * v));
                                        mix.offset += 2;
                                    } else {
                                        mix.write(0, (short) (mix.read(0) + (((w.read(offs >> 1) >> 4) & 0x0f) - 8) * v));
                                        mix.offset += 2;
                                    }
                                }
                            }
                        } else {
                            offs = (c >> 15) & 0x1f;

                            /* use full byte, first the high 4 bits, then the low 4 bits */
                            if ((offs & 1) != 0) {
                                mix.write(0, (short) (mix.read(0) + ((w.read(offs >> 1) & 0x0f) - 8) * v));
                                mix.offset += 2;
                            } else {
                                mix.write(0, (short) (mix.read(0) + (((w.read(offs >> 1) >> 4) & 0x0f) - 8) * v));
                                mix.offset += 2;
                            }
                        }
                    }

                    /* update the counter for this voice */
                    channel_list[voice].counter = c;
                }
            }

            /* mix it down */
            mix = new ShortPtr(mixer_buffer);
            for (int i = 0; i < length; i++) {
                buffer.write(0, mixer_lookup[mixer_lookup_middle + (short) mix.read(0)]);
                buffer.offset += 2;
                mix.offset += 2;
            }
        }
    };

    public static ShStartHandlerPtr wiping_sh_start = new ShStartHandlerPtr() {
        public int handler(MachineSound msound) {
            String mono_name = "Wiping";

            /* get stream channels */
            stream = stream_init(mono_name, 100/*intf.volume*/, samplerate, 0, wiping_update_mono);

            /* allocate a pair of buffers to mix into - 1 second's worth should be more than enough */
 /* allocate a pair of buffers to mix into - 1 second's worth should be more than enough */
            mixer_buffer = new ShortPtr(2 * samplerate * 2);
            mixer_buffer_2 = new ShortPtr(mixer_buffer, samplerate * 2);

            /* build the mixer table */
            if (make_mixer_table(8, defgain) != 0) {
                mixer_buffer = null;
                return 1;
            }

            /* extract globals from the interface */
            num_voices = 8;
            last_channel = num_voices;

            sound_rom = memory_region(REGION_SOUND1);
            sound_prom = memory_region(REGION_SOUND2);

            /* start with sound enabled, many games don't have a sound enable register */
            sound_enable = 1;

            /* reset all the voices */
            for (int i = 0; i < last_channel; i++) //for (voice = channel_list; voice < last_channel; voice++)
            {
                channel_list[i] = new sound_channel();
                channel_list[i].frequency = 0;
                channel_list[i].volume[0] = channel_list[i].volume[1] = 0;
                channel_list[i].wave = sound_prom;
                channel_list[i].counter = 0;
            }

            return 0;
        }
    };

    public static ShStopHandlerPtr wiping_sh_stop = new ShStopHandlerPtr() {
        public void handler() {
            //mixer_table=null;
            mixer_buffer = null;
        }
    };

    /**
     * *****************************************************************************
     */
    public static WriteHandlerPtr wiping_sound_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            int voice;
            int _base;

            /* update the streams */
            stream_update(stream, 0);

            /* set the register */
            wiping_soundregs.write(offset, data);

            /* recompute all the voice parameters */
            if (offset <= 0x3f) {
                for (_base = 0, voice = 0; voice < last_channel; voice++, _base += 8) {
                    channel_list[voice].frequency = wiping_soundregs.read(0x02 + _base) & 0x0f;
                    channel_list[voice].frequency = channel_list[voice].frequency * 16 + ((wiping_soundregs.read(0x01 + _base)) & 0x0f);
                    channel_list[voice].frequency = channel_list[voice].frequency * 16 + ((wiping_soundregs.read(0x00 + _base)) & 0x0f);

                    channel_list[voice].volume[0] = wiping_soundregs.read(0x07 + _base) & 0x0f;
                    if ((wiping_soundregs.read(0x5 + _base) & 0x0f) != 0) {
                        channel_list[voice].wave = new UBytePtr(sound_rom, 128 * (16 * (wiping_soundregs.read(0x5 + _base) & 0x0f)
                                + (wiping_soundregs.read(0x2005 + _base) & 0x0f)));
                        channel_list[voice].oneshot = 1;
                    } else {
                        channel_list[voice].wave = new UBytePtr(sound_rom, 16 * (wiping_soundregs.read(0x3 + _base) & 0x0f));
                        channel_list[voice].oneshot = 0;
                    }
                }
            } else if (offset >= 0x2000) {
                sound_channel _voice = channel_list[(offset & 0x3f) / 8];
                if (_voice.oneshot != 0) {
                    _voice.counter = 0;
                    _voice.oneshotplaying = 1;
                }
            }
        }
    };
}
