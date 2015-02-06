
package sound;
import mame.sndintrf;
import mame.sndintrfH;
import static mame.sndintrfH.*;
import static sound._3812intfH.*;
import static mame.driverH.*;
import static sound.fmoplH.*;

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
        chiptype = OPL_TYPE_Y8950;
	if( OPL_sh_start(msound)!=0 ) return 1;
	/* !!!!! port handler set !!!!! */
	/* !!!!! delta-t memory address set !!!!! */
	return 0;
    }

    public static ReadHandlerPtr Y8950_status_port_0_r = new ReadHandlerPtr() { public int handler(int offset)
    {
        return YM3812_status_port_0_r.handler(offset);
    }};
    public static ReadHandlerPtr Y8950_status_port_1_r = new ReadHandlerPtr() { public int handler(int offset)
    {
        return YM3812_status_port_1_r.handler(offset);
    }};
    public static WriteHandlerPtr Y8950_control_port_0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
        YM3812_control_port_0_w.handler(offset, data);    
    }};
    public static WriteHandlerPtr Y8950_write_port_0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
        YM3812_write_port_0_w.handler(offset, data);    
    }};
    public static WriteHandlerPtr Y8950_control_port_1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
         YM3812_control_port_1_w.handler(offset, data);   
    }};
    public static WriteHandlerPtr Y8950_write_port_1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
          YM3812_write_port_1_w.handler(offset, data);  
    }};    
}
