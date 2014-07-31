/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.09
 *
 *
 *
 */ 
package machine;
import static mame.driverH.*;
import static arcadeflex.libc.*;
import static mame.common.*;
import static mame.commonH.*;
import static mame.cpuintrf.*;
import static mame.inputport.*;
import static drivers.tp84.*;

public class tp84
{

	/* JB 970829 - just give it what it wants
		F104: LDX   $6400
		F107: LDU   $6402
		F10A: LDA   $640B
		F10D: BEQ   $F13B
		F13B: LDX   $6404
		F13E: LDU   $6406
		F141: LDA   $640C
		F144: BEQ   $F171
		F171: LDA   $2000	; read beam
		F174: ADDA  #$20
		F176: BCC   $F104
	*/
	public static ReadHandlerPtr tp84_beam_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	//	return cpu_getscanline();
		return 255; /* always return beam position 255 */ /* JB 970829 */
	} };
	
	/* JB 970829 - catch a busy loop for CPU 1
		E0ED: LDA   #$01
		E0EF: STA   $4000
		E0F2: BRA   $E0ED
	*/
	public static WriteHandlerPtr tp84_catchloop_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if( cpu_get_pc()==0xe0f2 ) cpu_spinuntil_int();
	} };
}
