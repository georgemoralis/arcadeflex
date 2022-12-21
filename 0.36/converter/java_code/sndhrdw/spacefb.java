/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package sndhrdw;

public class spacefb
{
	
	unsigned char spacefb_sound_latch;
	
	public static ReadHandlerPtr spacefb_sh_getp2 = new ReadHandlerPtr() { public int handler(int offset){
	    return ((spacefb_sound_latch & 0x18) << 1);
	} };
	
	public static ReadHandlerPtr spacefb_sh_gett0 = new ReadHandlerPtr() { public int handler(int offset){
	    return spacefb_sound_latch & 0x20;
	} };
	
	public static ReadHandlerPtr spacefb_sh_gett1 = new ReadHandlerPtr() { public int handler(int offset){
	    return spacefb_sound_latch & 0x04;
	} };
	
	public static WriteHandlerPtr spacefb_port_1_w = new WriteHandlerPtr() { public void handler(int offset, int data){
	    spacefb_sound_latch = data;
	    if (!(data & 0x02)) cpu_cause_interrupt(1,I8039_EXT_INT);
	} };
	
	public static WriteHandlerPtr spacefb_sh_putp1 = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		DAC_data_w(0,data);
	} };
	
}
