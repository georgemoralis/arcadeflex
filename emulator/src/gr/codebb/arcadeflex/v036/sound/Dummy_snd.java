
package gr.codebb.arcadeflex.v036.sound;

import gr.codebb.arcadeflex.v036.mame.sndintrf;
import gr.codebb.arcadeflex.v036.mame.sndintrfH;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;

public class Dummy_snd extends sndintrf.snd_interface
{
    public Dummy_snd()
    {
        sound_num=SOUND_DUMMY;
        name="";
    }
    @Override
    public int chips_num(MachineSound msound) {
        return 0;
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
