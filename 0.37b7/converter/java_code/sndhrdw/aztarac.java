/*
 * Aztarac soundboard interface emulation
 *
 * Jul 25 1999 by Mathis Rosenhauer
 *
 */

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package sndhrdw;

public class aztarac
{
	
	static int sound_command, sound_status;
	
	public static ReadHandlerPtr aztarac_sound_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    if (Machine.sample_rate)
	        return sound_status & 0x01;
	    else
	        return 1;
	} };
	
	public static WriteHandlerPtr aztarac_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    sound_command = data;
	    sound_status ^= 0x21;
	    if ((sound_status & 0x20) != 0)
	        cpu_cause_interrupt( 1, Z80_IRQ_INT );
	} };
	
	public static ReadHandlerPtr aztarac_snd_command_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    sound_status |= 0x01;
	    sound_status &= ~0x20;
	    return sound_command;
	} };
	
	public static ReadHandlerPtr aztarac_snd_status_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return sound_status & ~0x01;
	} };
	
	public static WriteHandlerPtr aztarac_snd_status_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    sound_status &= ~0x10;
	} };
	
	public static InterruptPtr aztarac_snd_timed_irq = new InterruptPtr() { public int handler() 
	{
	    sound_status ^= 0x10;
	
	    if ((sound_status & 0x10) != 0)
	        return Z80_IRQ_INT;
	    else
	        return Z80_IGNORE_INT;
	} };
}
