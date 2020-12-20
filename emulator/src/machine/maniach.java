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

import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.sndintrfH.*;
import static mame.cpuintrf.*;
import static mame.common.*;
import static platform.input.*;
import static mame.inputportH.*;
import static mame.inputH.*;
import static platform.libc.*;
import static platform.libc_old.*;
import static mame.memory.*;
import static mame.mame.*;


public class maniach
{
	
	
	static /*unsigned*/ char from_main,from_mcu;
	static int mcu_sent = 0,main_sent = 0;
	
	
	/***************************************************************************
	
	 Mania Challenge 68705 protection interface
	
	 The following is ENTIRELY GUESSWORK!!!
	
	***************************************************************************/
	
	static /*unsigned*/ char portA_in,portA_out,ddrA;
	
	public static ReadHandlerPtr maniach_68705_portA_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	//if (errorlog != 0) fprintf(errorlog,"%04x: 68705 port A read %02x\n",cpu_get_pc(),portA_in);
		return (portA_out & ddrA) | (portA_in & ~ddrA);
	} };
	
	public static WriteHandlerPtr maniach_68705_portA_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//if (errorlog != 0) fprintf(errorlog,"%04x: 68705 port A write %02x\n",cpu_get_pc(),data);
		portA_out = (char)(data&0xFF);
	} };
	
	public static WriteHandlerPtr maniach_68705_ddrA_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		ddrA = (char)(data&0xFF);
	} };
	
	
	
	/*
	 *  Port B connections:
	 *
	 *  all bits are logical 1 when read (+5V pullup)
	 *
	 *  1   W  when 1.0, enables latch which brings the command from main CPU (read from port A)
	 *  2   W  when 0.1, copies port A to the latch for the main CPU
	 */
	
	static /*unsigned*/ char portB_in,portB_out,ddrB;
	
	public static ReadHandlerPtr maniach_68705_portB_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (portB_out & ddrB) | (portB_in & ~ddrB);
	} };
	
	public static WriteHandlerPtr maniach_68705_portB_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//if (errorlog != 0) fprintf(errorlog,"%04x: 68705 port B write %02x\n",cpu_get_pc(),data);
	
		if ((ddrB & 0x02)!=0 && (~data & 0x02)!=0 && (portB_out & 0x02)!=0)
		{
			portA_in = from_main;
			main_sent = 0;
	//if (errorlog != 0) fprintf(errorlog,"read command %02x from main cpu\n",portA_in);
		}
		if ((ddrB & 0x04)!=0 && (data & 0x04)!=0 && (~portB_out & 0x04)!=0)
		{
	//if (errorlog != 0) fprintf(errorlog,"send command %02x to main cpu\n",portA_out);
			from_mcu = portA_out;
			mcu_sent = 1;
		}
	
		portB_out = (char)(data&0xFF);;
	} };
	
	public static WriteHandlerPtr maniach_68705_ddrB_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		ddrB = (char)(data&0xFF);
	} };
	
	
	static /*unsigned*/ char portC_in,portC_out,ddrC;
	
	public static ReadHandlerPtr maniach_68705_portC_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		portC_in = 0;
		if (main_sent != 0) portC_in |= 0x01;
		if (mcu_sent==0) portC_in |= 0x02;
	//if (errorlog != 0) fprintf(errorlog,"%04x: 68705 port C read %02x\n",cpu_get_pc(),portC_in);
		return (portC_out & ddrC) | (portC_in & ~ddrC);
	} };
	
	public static WriteHandlerPtr maniach_68705_portC_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//if (errorlog != 0) fprintf(errorlog,"%04x: 68705 port C write %02x\n",cpu_get_pc(),data);
		portC_out = (char)(data&0xFF);
	} };
	
	public static WriteHandlerPtr maniach_68705_ddrC_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		ddrC = (char)(data&0xFF);
	} };
	
	
	public static WriteHandlerPtr maniach_mcu_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//if (errorlog != 0) fprintf (errorlog, "%04x: 3040_w %02x\n",cpu_get_pc(),data);
		from_main = (char)(data&0xFF);
		main_sent = 1;
	} };
	
	public static ReadHandlerPtr maniach_mcu_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	//if (errorlog != 0) fprintf (errorlog, "%04x: 3040_r %02x\n",cpu_get_pc(),from_mcu);
		mcu_sent = 0;
		return from_mcu;
	} };
	
	public static ReadHandlerPtr maniach_mcu_status_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res = 0;
	
		/* bit 0 = when 0, mcu has sent data to the main cpu */
		/* bit 1 = when 1, mcu is ready to receive data from main cpu */
	//if (errorlog != 0) fprintf (errorlog, "%04x: 3041_r\n",cpu_get_pc());
		if (mcu_sent==0) res |= 0x01;
		if (main_sent==0) res |= 0x02;
	
		return res;
	} };
}
