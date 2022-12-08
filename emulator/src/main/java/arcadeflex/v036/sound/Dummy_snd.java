/*
 * ported to v0.36
 * 
 */
package arcadeflex.v036.sound;

//mame imports
import static arcadeflex.v036.mame.sndintrfH.*;
//TODO
import gr.codebb.arcadeflex.v036.mame.sndintrf.snd_interface;

public class Dummy_snd extends snd_interface {

    public Dummy_snd() {
        sound_num = SOUND_DUMMY;
        name = "";
    }

    @Override
    public int chips_num(MachineSound msound) {
        return 0;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int start(MachineSound msound) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
