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

public class starfire
{
	
	static int sound_ctrl;
	static int fireone_sell;
	
	/* In drivers/starfire.c */
	extern unsigned char *starfire_ram;
	
	/* In vidhrdw/starfire.c */
	extern public static WriteHandlerPtr starfire_vidctrl_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	extern public static WriteHandlerPtr starfire_vidctrl1_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	
	public static WriteHandlerPtr starfire_shadow_w = new WriteHandlerPtr() { public void handler(int address, int data)
	{
	    starfire_ram[address & 0x3ff] = data;
	} };
	
	public static WriteHandlerPtr starfire_output_w = new WriteHandlerPtr() { public void handler(int address, int data)
	{
	    starfire_ram[address & 0x3ff] = data;
	    switch(address & 0xf) {
	    case 0:
			starfire_vidctrl_w(0, data);
			break;
	    case 1:
			starfire_vidctrl1_w(0, data);
			break;
	    case 2:
			/* Sounds */
			break;
	    }
	} };
	
	public static WriteHandlerPtr fireone_output_w = new WriteHandlerPtr() { public void handler(int address, int data)
	{
	    starfire_ram[address & 0x3ff] = data;
	    switch(address & 0xf) {
	    case 0:
			starfire_vidctrl_w(0, data);
			break;
	    case 1:
			starfire_vidctrl1_w(0, data);
			break;
	    case 2:
			/* Sounds */
			fireone_sell = (data & 0x8) ? 0 : 1;
			break;
	    }
	} };
	
	public static ReadHandlerPtr starfire_shadow_r = new ReadHandlerPtr() { public int handler(int address)
	{
	    return starfire_ram[address & 0x3ff];
	} };
	
	public static ReadHandlerPtr starfire_input_r = new ReadHandlerPtr() { public int handler(int address)
	{
	    switch(address & 0xf) {
	    case 0:
			return input_port_0_r(0);
	    case 1:
			/* Note : need to loopback sounds lengths on that one */
			return input_port_1_r(0);
	    case 5:
			/* Throttle, should be analog too */
			return input_port_4_r(0);
	    case 6:
			return input_port_2_r(0);
	    case 7:
			return input_port_3_r(0);
	    default:
			return 0xff;
	    }
	} };
	
	public static ReadHandlerPtr fireone_input_r = new ReadHandlerPtr() { public int handler(int address)
	{
	    switch(address & 0xf) {
	    case 0:
			return input_port_0_r(0);
	    case 1:
			return input_port_1_r(0);
	    case 2:
			/* Throttle, should be analog too */
			return fireone_sell ? input_port_2_r(0) : input_port_3_r(0);
	    default:
			return 0xff;
	    }
	} };
	
	public static WriteHandlerPtr starfire_soundctrl_w = new WriteHandlerPtr() { public void handler(int offset, int data){
	    sound_ctrl = data;
	} };
	
	public static ReadHandlerPtr starfire_io1_r = new ReadHandlerPtr() { public int handler(int offset){
	    int in,out;
	
	    in = readinputport(1);
	    out = (in & 0x07) | 0xE0;
	
	    if ((sound_ctrl & 0x04) != 0)
	        out = out | 0x08;
	    else
	        out = out & 0xF7;
	
	    if ((sound_ctrl & 0x08) != 0)
	        out = out | 0x10;
	    else
	        out = out & 0xEF;
	
	    return out;
	} };
	
	public static InterruptPtr starfire_interrupt = new InterruptPtr() { public int handler() 
	{
	
	    return nmi_interrupt();
	} };
}
