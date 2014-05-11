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
import static arcadeflex.input.*;
import static mame.inputportH.*;
import static mame.inputH.*;
import static arcadeflex.libc.*;
import static arcadeflex.libc_old.*;
import static mame.memory.*;
import static mame.mame.*;
import static mame.cpuintrfH.*;
import static mame.sndintrf.*;
import static cpu.z80.z80H.*;

public class espial
{
	
	
	public static InitMachinePtr espial_init_machine = new InitMachinePtr() { public void handler() 
	{
		/* we must start with NMI interrupts disabled */
		//interrupt_enable = 0;
		interrupt_enable_w.handler(0, 0);
	} };
	
	
	public static WriteHandlerPtr zodiac_master_interrupt_enable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		interrupt_enable_w.handler(offset, data ^ 1);
	} };
	
	
	public static InterruptPtr zodiac_master_interrupt = new InterruptPtr() { public int handler() 
	{
		return (cpu_getiloops() == 0) ? nmi_interrupt.handler(): interrupt.handler();
	} };
	
	
	public static WriteHandlerPtr zodiac_master_soundlatch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		soundlatch_w.handler(offset, data);
		cpu_cause_interrupt(1, Z80_IRQ_INT);
	} };
	
}
