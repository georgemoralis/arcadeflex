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

public class cloak
{
	
	unsigned char *cloak_sharedram;
	unsigned char *cloak_nvRAM;
	unsigned char *enable_nvRAM;
	
	public static ReadHandlerPtr cloak_sharedram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return cloak_sharedram[offset];
	} };
	
	public static WriteHandlerPtr cloak_sharedram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cloak_sharedram[offset] = data;
	} };
}
