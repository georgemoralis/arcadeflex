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
import static arcadeflex.input.*;
import static mame.inputportH.*;
import static mame.inputH.*;
import static arcadeflex.libc.*;
import static arcadeflex.libc_old.*;
import static mame.memory.*;
import static mame.mame.*;
import static cpu.z80.z80H.*;

public class docastle
{
	
	
	
	static char[] buffer0= new char[9];/*unsigned!*/
        static char[] buffer1= new char[9];/*unsigned!*/
	
	
	
	public static ReadHandlerPtr docastle_shared0_r = new ReadHandlerPtr() { public int handler(int offset)
	{
            if (errorlog!=null && offset == 8) fprintf(errorlog,"CPU #0 shared0r  clock = %d\n",cpu_gettotalcycles());
	
		/* this shouldn't be done, however it's the only way I've found */
		/* to make dip switches work in Do Run Run. */
		if (offset == 8)
		{
			cpu_cause_interrupt(1,Z80_NMI_INT);
			cpu_spinuntil_trigger(500);
		}
	
		return buffer0[offset];
	} };
	
	
	public static ReadHandlerPtr docastle_shared1_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	if (errorlog!=null && offset == 8) fprintf(errorlog,"CPU #1 shared1r  clock = %d\n",cpu_gettotalcycles());
		return buffer1[offset];
	} };
	
	
	public static WriteHandlerPtr docastle_shared0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	if (errorlog!=null && offset == 8) fprintf(errorlog,"CPU #1 shared0w %02x %02x %02x %02x %02x %02x %02x %02x %02x clock = %d\n",
			buffer0[0],buffer0[1],buffer0[2],buffer0[3],buffer0[4],buffer0[5],buffer0[6],buffer0[7],data,cpu_gettotalcycles());
	
		buffer0[offset] = (char)(data);
	
		if (offset == 8)
			/* awake the master CPU */
			cpu_trigger.handler(500);
	} };
	
	
	public static WriteHandlerPtr docastle_shared1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		buffer1[offset] = (char)(data);
	
		if (offset == 8)
		{
			if (errorlog != null) fprintf(errorlog,"CPU #0 shared1w %02x %02x %02x %02x %02x %02x %02x %02x %02x clock = %d\n",
					buffer1[0],buffer1[1],buffer1[2],buffer1[3],buffer1[4],buffer1[5],buffer1[6],buffer1[7],data,cpu_gettotalcycles());
	
			/* freeze execution of the master CPU until the slave has used the shared memory */
			cpu_spinuntil_trigger(500);
		}
	} };
	
	
	
	public static WriteHandlerPtr docastle_nmitrigger = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_cause_interrupt(1,Z80_NMI_INT);
	} };
}
