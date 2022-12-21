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

public class espial
{
	
	
	public static InitMachinePtr espial_init_machine = new InitMachinePtr() { public void handler() 
	{
		/* we must start with NMI interrupts disabled */
		//interrupt_enable = 0;
		interrupt_enable_w(0, 0);
	} };
	
	
	public static WriteHandlerPtr zodiac_master_interrupt_enable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		interrupt_enable_w(offset, data ^ 1);
	} };
	
	
	public static InterruptPtr zodiac_master_interrupt = new InterruptPtr() { public int handler() 
	{
		return (cpu_getiloops() == 0) ? nmi_interrupt() : interrupt();
	} };
	
	
	public static WriteHandlerPtr zodiac_master_soundlatch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		soundlatch_w(offset, data);
		cpu_cause_interrupt(1, Z80_IRQ_INT);
	} };
	
}
