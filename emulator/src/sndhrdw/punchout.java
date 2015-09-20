/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package sndhrdw;
import static mame.driverH.*;
import static mame.cpuintrf.*;
import static mame.cpuintrfH.*;
import static mame.sndintrf.*;
import static mame.inputport.*;
import static sound.vlm5030.*;
import static sound.vlm5030H.*;

public class punchout
{
	
	public static ReadHandlerPtr punchout_input_3_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int data = input_port_3_r.handler(offset);
		/* bit 4 is busy pin level */
		if( VLM5030_BSY()!=0 ) data &= ~0x10;
		else data |= 0x10;
		return data;
	} };
	
	public static WriteHandlerPtr punchout_speech_reset = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		VLM5030_RST( data&0x01 );
	} };
	
	public static WriteHandlerPtr punchout_speech_st = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		VLM5030_ST( data&0x01 );
	} };
	
	public static WriteHandlerPtr punchout_speech_vcu = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		VLM5030_VCU( data & 0x01 );
	} };
	
}
