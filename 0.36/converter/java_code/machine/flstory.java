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

public class flstory
{
	
	
	static unsigned char from_main,from_mcu;
	static int mcu_sent = 0,main_sent = 0;
	
	
	/***************************************************************************
	
	 Fairy Land Story 68705 protection interface
	
	 The following is ENTIRELY GUESSWORK!!!
	
	***************************************************************************/
	
	static unsigned char portA_in,portA_out,ddrA;
	
	public static ReadHandlerPtr flstory_68705_portA_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	//if (errorlog != 0) fprintf(errorlog,"%04x: 68705 port A read %02x\n",cpu_get_pc(),portA_in);
		return (portA_out & ddrA) | (portA_in & ~ddrA);
	} };
	
	public static WriteHandlerPtr flstory_68705_portA_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//if (errorlog != 0) fprintf(errorlog,"%04x: 68705 port A write %02x\n",cpu_get_pc(),data);
		portA_out = data;
	} };
	
	public static WriteHandlerPtr flstory_68705_ddrA_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		ddrA = data;
	} };
	
	
	
	/*
	 *  Port B connections:
	 *
	 *  all bits are logical 1 when read (+5V pullup)
	 *
	 *  1   W  when 1.0, enables latch which brings the command from main CPU (read from port A)
	 *  2   W  when 0.1, copies port A to the latch for the main CPU
	 */
	
	static unsigned char portB_in,portB_out,ddrB;
	
	public static ReadHandlerPtr flstory_68705_portB_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (portB_out & ddrB) | (portB_in & ~ddrB);
	} };
	
	public static WriteHandlerPtr flstory_68705_portB_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//if (errorlog != 0) fprintf(errorlog,"%04x: 68705 port B write %02x\n",cpu_get_pc(),data);
	
		if ((ddrB & 0x02) && (~data & 0x02) && (portB_out & 0x02))
		{
			portA_in = from_main;
			if (main_sent != 0) cpu_set_irq_line(2,0,CLEAR_LINE);
			main_sent = 0;
	if (errorlog != 0) fprintf(errorlog,"read command %02x from main cpu\n",portA_in);
		}
		if ((ddrB & 0x04) && (data & 0x04) && (~portB_out & 0x04))
		{
	if (errorlog != 0) fprintf(errorlog,"send command %02x to main cpu\n",portA_out);
			from_mcu = portA_out;
			mcu_sent = 1;
		}
	
		portB_out = data;
	} };
	
	public static WriteHandlerPtr flstory_68705_ddrB_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		ddrB = data;
	} };
	
	
	static unsigned char portC_in,portC_out,ddrC;
	
	public static ReadHandlerPtr flstory_68705_portC_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		portC_in = 0;
	//	if (main_sent != 0) portC_in |= 0x01;
		if (!mcu_sent) portC_in |= 0x02;
	if (errorlog != 0) fprintf(errorlog,"%04x: 68705 port C read %02x\n",cpu_get_pc(),portC_in);
		return (portC_out & ddrC) | (portC_in & ~ddrC);
	} };
	
	public static WriteHandlerPtr flstory_68705_portC_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	if (errorlog != 0) fprintf(errorlog,"%04x: 68705 port C write %02x\n",cpu_get_pc(),data);
		portC_out = data;
	} };
	
	public static WriteHandlerPtr flstory_68705_ddrC_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		ddrC = data;
	} };
	
	
	public static WriteHandlerPtr flstory_mcu_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	if (errorlog != 0) fprintf (errorlog, "%04x: mcu_w %02x\n",cpu_get_pc(),data);
		from_main = data;
		main_sent = 1;
		cpu_set_irq_line(2,0,ASSERT_LINE);
	} };
	
	public static ReadHandlerPtr flstory_mcu_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	if (errorlog != 0) fprintf (errorlog, "%04x: mcu_r %02x\n",cpu_get_pc(),from_mcu);
		mcu_sent = 0;
		return from_mcu;
	} };
	
	public static ReadHandlerPtr flstory_mcu_status_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res = 0;
	
		/* bit 0 = when 1, mcu is ready to receive data from main cpu */
		/* bit 1 = when 1, mcu has sent data to the main cpu */
	//if (errorlog != 0) fprintf (errorlog, "%04x: mcu_status_r\n",cpu_get_pc());
		if (!main_sent) res |= 0x01;
		if (mcu_sent != 0) res |= 0x02;
	
		return res;
	} };
}
