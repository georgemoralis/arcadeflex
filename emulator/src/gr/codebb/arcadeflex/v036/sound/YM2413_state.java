package gr.codebb.arcadeflex.v036.sound;

import static gr.codebb.arcadeflex.v036.sound.ym2413.*;

public class YM2413_state {

    public YM2413_state() {
        user_instrument = new int[ym2413_parameter_count];
    }
    public int rhythm_mode;
    public int pending_register;
    public int[] user_instrument;
}
