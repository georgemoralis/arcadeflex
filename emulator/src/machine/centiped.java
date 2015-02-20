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
import static mame.inputport.*;
public class centiped
{
	
	/*
	 * This wrapper routine is necessary because Centipede requires a direction bit
	 * to be set or cleared. The direction bit is held until the mouse is moved
	 * again.
	 *
	 * There is a 4-bit counter, and two inputs from the trackball: DIR and CLOCK.
	 * CLOCK makes the counter move in the direction of DIR. Since DIR is latched
	 * only when a CLOCK arrives, the DIR bit in the input port doesn't change
	 * until the trackball actually moves.
	 *
	 * There is also a CLR input to the counter which could be used by the game to
	 * clear the counter, but Centipede doesn't use it (though it would be a good
	 * idea to support it anyway).
	 *
	 * The counter is read 240 times per second. There is no provision whatsoever
	 * to prevent the counter from wrapping around between reads.
	 */
         static int oldpos,sign;
	public static ReadHandlerPtr centiped_IN0_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		
		int newpos;
	
		newpos = readinputport(6);
		if (newpos != oldpos)
		{
			sign = (newpos - oldpos) & 0x80;
			oldpos = newpos;
		}
	
		return ((readinputport(0) & 0x70) | (oldpos & 0x0f) | sign );
	} };
	static int oldpos2,sign2;
	public static ReadHandlerPtr centiped_IN2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		
		int newpos;
	
		newpos = readinputport(2);
		if (newpos != oldpos2)
		{
			sign2 = (newpos - oldpos2) & 0x80;
			oldpos2 = newpos;
		}
	
		return ((oldpos2 & 0x0f) | sign2 );
	} };
}
