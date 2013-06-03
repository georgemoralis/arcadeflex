
package sound;

import mame.sndintrf;
import mame.sndintrfH;
import static mame.sndintrfH.*;

public class Dummy_snd extends sndintrf.snd_interface
{
    public Dummy_snd()
    {
        sound_num=SOUND_DUMMY;
        name="";
    }
    @Override
    public int chips_num(sndintrfH.MachineSound msound) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public int chips_clock(sndintrfH.MachineSound msound) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public int start(sndintrfH.MachineSound msound) {
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
