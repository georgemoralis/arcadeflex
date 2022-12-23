/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 */
/**
 * Changelog
 * =========
 * 23/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.sndhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//sound imports
import static arcadeflex.v036.sound.samples.*;

public class congo {

    public static WriteHandlerPtr congo_daio = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 1) {
                if ((data & 2) != 0) {
                    sample_start(0, 0, 0);
                }
            } else if (offset == 2) {
                data ^= 0xff;

                if ((data & 0x80) != 0) {
                    if ((data & 8) != 0) {
                        sample_start(1, 1, 0);
                    }
                    if ((data & 4) != 0) {
                        sample_start(2, 2, 0);
                    }
                    if ((data & 2) != 0) {
                        sample_start(3, 3, 0);
                    }
                    if ((data & 1) != 0) {
                        sample_start(4, 4, 0);
                    }
                }
            }
        }
    };
}
