/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.08
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
import static mame.cpuintrfH.*;

public class scramble
{
	
	
	public static ReadHandlerPtr scramble_input_port_2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res;
	
	
		res = readinputport(2);
	
	/*if (errorlog != 0) fprintf(errorlog,"%04x: read IN2\n",cpu_get_pc());*/
	
		/* avoid protection */
		if (cpu_get_pc() == 0x00e4) res &= 0x7f;
	
		return res;
	} };
	
	
	
	public static ReadHandlerPtr scramble_protection_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (errorlog != null) fprintf(errorlog,"%04x: read protection\n",cpu_get_pc());
	
		return 0x6f;
	} };
	
	public static ReadHandlerPtr scramblk_protection_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (cpu_get_pc())
		{
		case 0x00a8: return 0xf0;
		case 0x00be: return 0xb0;
		case 0x0c1d: return 0xf0;
		case 0x0c6a: return 0xb0;
		case 0x0ceb: return 0x40;
		case 0x0d37: return 0x60;
		case 0x1ca2: return 0x00;  /* I don't think it's checked */
		case 0x1d7e: return 0xb0;
		default:
			if (errorlog != null) fprintf(errorlog,"%04x: read protection\n",cpu_get_pc());
			return 0;
		}
	} };
	
	public static ReadHandlerPtr scramblb_protection_1_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (cpu_get_pc())
		{
		case 0x01da: return 0x80;
		case 0x01e4: return 0x00;
		default:
			if (errorlog != null) fprintf(errorlog,"%04x: read protection 1\n",cpu_get_pc());
			return 0;
		}
	} };
	
	public static ReadHandlerPtr scramblb_protection_2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (cpu_get_pc())
		{
		case 0x01ca: return 0x90;
		default:
			if (errorlog != null) fprintf(errorlog,"%04x: read protection 2\n",cpu_get_pc());
			return 0;
		}
	} };
	
	
	public static ReadHandlerPtr mariner_protection_1_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 7;
	} };
	public static ReadHandlerPtr mariner_protection_2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 3;
	} };
	
	
	public static ReadHandlerPtr mariner_pip = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (errorlog != null) fprintf(errorlog,"PC %04x: read port 2\n",cpu_get_pc());
		if (cpu_get_pc() == 0x015a) return 0xff;
		else if (cpu_get_pc() == 0x0886) return 0x05;
		else return 0;
	} };
	
	public static ReadHandlerPtr mariner_pap = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (errorlog != null) fprintf(errorlog,"PC %04x: read port 3\n",cpu_get_pc());
		if (cpu_get_pc() == 0x015d) return 0x04;
		else return 0;
	} };
	
	
}
