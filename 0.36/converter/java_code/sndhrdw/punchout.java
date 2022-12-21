/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package sndhrdw;

public class punchout
{
	
	public static ReadHandlerPtr punchout_input_3_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int data = input_port_3_r(offset);
		/* bit 4 is busy pin level */
		if( VLM5030_BSY() ) data &= ~0x10;
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
