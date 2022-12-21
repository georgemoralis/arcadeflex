/***************************************************************************

  liberator.c - 'machine.c'

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

public class liberatr
{
	
	
	UINT8 *liberatr_ctrld;
	
	
	public static WriteHandlerPtr liberatr_led_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		osd_led_w(offset, (data >> 4) & 0x01);
	} };
	
	
	public static WriteHandlerPtr liberatr_coin_counter_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		coin_counter_w(offset ^ 0x01, data);
	} };
	
	
	public static ReadHandlerPtr liberatr_input_port_0_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int	res ;
		int xdelta, ydelta;
	
	
		/* CTRLD selects whether we're reading the stick or the coins,
		   see memory map */
	
		if(*liberatr_ctrld)
		{
			/* 	mouse support */
			xdelta = input_port_4_r(0);
			ydelta = input_port_5_r(0);
			res = ( ((ydelta << 4) & 0xf0)  |  (xdelta & 0x0f) );
		}
		else
		{
			res = input_port_0_r(offset);
		}
	
		return res;
	} };
}
