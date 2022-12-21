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

public class vastar
{
	
	
	
	unsigned char *vastar_sharedram;
	
	
	
	public static InitMachinePtr vastar_init_machine = new InitMachinePtr() { public void handler() 
	{
		/* we must start with the second CPU halted */
		cpu_set_reset_line(1,ASSERT_LINE);
	} };
	
	public static WriteHandlerPtr vastar_hold_cpu2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* I'm not sure that this works exactly like this */
		if ((data & 1) != 0)
			cpu_set_reset_line(1,CLEAR_LINE);
		else
			cpu_set_reset_line(1,ASSERT_LINE);
	} };
	
	
	
	public static ReadHandlerPtr vastar_sharedram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return vastar_sharedram[offset];
	} };
	
	public static WriteHandlerPtr vastar_sharedram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		vastar_sharedram[offset] = data;
	} };
}
