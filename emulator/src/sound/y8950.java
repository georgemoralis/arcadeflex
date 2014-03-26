
package sound;
import mame.sndintrf;
import mame.sndintrfH;
import static mame.sndintrfH.*;
import static sound._3812intfH.*;
import static mame.driverH.*;

public class y8950 extends _3812intf
{
        public y8950()
    {
        sound_num=SOUND_Y8950;
        name="Y8950";
    }
    @Override
    public int chips_num(sndintrfH.MachineSound msound) {
        return ((Y8950interface)msound.sound_interface).num;
    }

    @Override
    public int chips_clock(sndintrfH.MachineSound msound) {
        return ((Y8950interface)msound.sound_interface).baseclock;
    }

    @Override
    public int start(sndintrfH.MachineSound msound) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return 0;//ttemp
    }

    @Override
    public void stop() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }
    public static ReadHandlerPtr Y8950_status_port_0_r = new ReadHandlerPtr() { public int handler(int offset)
    {
        return 0;//for now
    }};
    public static ReadHandlerPtr Y8950_status_port_1_r = new ReadHandlerPtr() { public int handler(int offset)
    {
        return 0;//for now
    }};
    public static WriteHandlerPtr Y8950_control_port_0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
            
    }};
    public static WriteHandlerPtr Y8950_write_port_0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
            
    }};
    public static WriteHandlerPtr Y8950_control_port_1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
            
    }};
    public static WriteHandlerPtr Y8950_write_port_1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
            
    }};    
}
