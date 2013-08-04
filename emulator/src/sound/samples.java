
package sound;

import mame.sndintrf;
import mame.sndintrfH;
import static mame.sndintrfH.*;
import static sound.dacH.*;

public class samples extends sndintrf.snd_interface
{
    public samples()
    {
        sound_num=SOUND_SAMPLES;
        name="Samples";
    }
    @Override
    public int chips_num(MachineSound msound) {
        return 0;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return 0;
    }

    @Override
    public int start(MachineSound msound) {
       //TODO
        return 0;
    }

    @Override
    public void stop() {
       
    }

    @Override
    public void update() {
        
    }

    @Override
    public void reset() {
        
    }
    public static void sample_start(int channel,int samplenum,int loop)
    {
        
    }
}
