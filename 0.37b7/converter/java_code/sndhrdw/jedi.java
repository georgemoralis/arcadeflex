/***************************************************************************

sndhrdw\jedi.c

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package sndhrdw;

public class jedi
{
	
	/* Misc sound code */
	
	public static WriteHandlerPtr jedi_speech_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    static unsigned char speech_write_buffer;
	
	    if(offset<0xff)
	    {
	        speech_write_buffer = data;
	    }
	    else if (offset<0x1ff)
	    {
	        tms5220_data_w(0,speech_write_buffer);
	    }
	} };
	
	public static ReadHandlerPtr jedi_speech_ready_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return (!tms5220_ready_r())<<7;
	} };
}
