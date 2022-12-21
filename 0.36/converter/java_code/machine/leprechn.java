/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package machine;

public class leprechn
{
	
	static int input_port_select;
	
	public static WriteHandlerPtr leprechn_input_port_select_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	    input_port_select = data;
	} };
	
	
	public static ReadHandlerPtr leprechn_input_port_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	    switch (input_port_select)
	    {
	    case 0x01:
	        return input_port_0_r(0);
	    case 0x02:
	        return input_port_2_r(0);
	    case 0x04:
	        return input_port_3_r(0);
	    case 0x08:
	        return input_port_1_r(0);
	    case 0x40:
	        return input_port_5_r(0);
	    case 0x80:
	        return input_port_4_r(0);
	    }
	
	    return 0xff;
	} };
	
	
	public static ReadHandlerPtr leprechn_200d_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	    // Maybe a VSYNC line?
	    return 0x02;
	} };
	
	
	public static ReadHandlerPtr leprechn_0805_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return 0xc0;
	} };
}
