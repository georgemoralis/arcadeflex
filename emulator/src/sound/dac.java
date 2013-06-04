
package sound;

import mame.sndintrf;
import mame.sndintrfH;
import static mame.sndintrfH.*;
import static sound.dacH.*;

public class dac extends sndintrf.snd_interface
{
    public dac()
    {
        sound_num=SOUND_DAC;
        name="DAC";
    }
    @Override
    public int chips_num(MachineSound msound) {
        return ((DACinterface)msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }   
}
