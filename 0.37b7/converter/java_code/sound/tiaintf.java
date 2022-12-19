/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package sound;

public class tiaintf
{
	
	static int channel = -1;
	static const struct TIAinterface *intf;
	
	public static ShStartPtr tia_sh_start = new ShStartPtr() { public int handler(MachineSound msound) 
	{
	    intf = msound.sound_interface;
	
	    if (Machine.sample_rate == 0)
	        return 0;
	
		channel = stream_init("TIA", intf.volume, Machine.sample_rate, 0, tia_process);
		if (channel == -1)
	        return 1;
	
		tia_sound_init(intf.clock, Machine.sample_rate, intf.gain);
	
	    return 0;
	} };
	
	
	public static ShStopPtr tia_sh_stop = new ShStopPtr() { public void handler() 
	{
	    /* Nothing to do here */
	} };
	
	public static ShUpdatePtr tia_sh_update = new ShUpdatePtr() { public void handler() 
	{
		stream_update(channel, 0);
	} };
	
	
}
