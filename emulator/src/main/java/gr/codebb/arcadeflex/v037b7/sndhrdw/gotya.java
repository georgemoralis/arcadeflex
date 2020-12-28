/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.sndhrdw;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.sound.samples.*;

public class gotya {

    static class gotya_sample {

        int sound_command;
        int channel;
        int looping;

        public gotya_sample(int sound_command, int channel, int looping) {
            this.sound_command = sound_command;
            this.channel = channel;
            this.looping = looping;
        }

    }

    static gotya_sample[] gotya_samples
            = {
                new gotya_sample(0x01, 0, 0),
                new gotya_sample(0x02, 1, 0),
                new gotya_sample(0x03, 2, 0),
                new gotya_sample(0x05, 2, 0),
                new gotya_sample(0x06, 3, 0),
                new gotya_sample(0x07, 3, 0),
                new gotya_sample(0x08, 0, 1),
                new gotya_sample(0x0a, 0, 0),
                new gotya_sample(0x0b, 0, 0),
                /* all the speech can go to one channel? */
                new gotya_sample(0x10, 3, 0),
                new gotya_sample(0x11, 3, 0),
                new gotya_sample(0x12, 0, 0), /* this should stop the main tune */
                new gotya_sample(0x13, 3, 0),
                new gotya_sample(0x14, 3, 0),
                new gotya_sample(0x15, 3, 0),
                new gotya_sample(0x16, 3, 0),
                new gotya_sample(0x17, 3, 0),
                new gotya_sample(0x18, 3, 0),
                new gotya_sample(0x19, 3, 0),
                new gotya_sample(0x1a, 3, 0),
                new gotya_sample(-1, 0, 0) /* end of array */};
    static int theme_playing;
    public static WriteHandlerPtr gotya_soundlatch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            int sample_number;

            if (data == 0) {
                sample_stop(0);
                theme_playing = 0;
                return;
            }

            /* search for sample to play */
            for (sample_number = 0;
                    gotya_samples[sample_number].sound_command != -1;
                    sample_number++) {
                if (gotya_samples[sample_number].sound_command == data) {
                    if (gotya_samples[sample_number].looping != 0
                            && theme_playing != 0) {
                        /* don't restart main theme */
                        return;
                    }

                    sample_start(gotya_samples[sample_number].channel,
                            sample_number,
                            gotya_samples[sample_number].looping);

                    if (gotya_samples[sample_number].channel == 0) {
                        theme_playing = gotya_samples[sample_number].looping;
                    }
                    return;
                }
            }
        }
    };
}
