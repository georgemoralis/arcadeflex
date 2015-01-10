package sound;

import static arcadeflex.libc_old.*;
import static arcadeflex.ptrlib.*;
import static mame.sndintrfH.*;
import static sound.pokeyH.*;
import static mame.mame.*;
import static sound.streams.*;
import static mame.driverH.*;
import static mame.common.*;
import static mame.sndintrf.*;

public class pokey extends snd_interface {

    public pokey() {
        this.sound_num = SOUND_POKEY;
        this.name = "Pokey";
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((POKEYinterface)msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((POKEYinterface)msound.sound_interface).baseclock;
    }

    @Override
    public int start(MachineSound msound) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }

}
