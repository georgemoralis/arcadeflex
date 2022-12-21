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

public class jrpacman
{
	
	
	static int speedcheat = 0;	/* a well known hack allows to make JrPac Man run at four times */
					/* his usual speed. When we start the emulation, we check if the */
					/* hack can be applied, and set this flag accordingly. */
	
	
	public static InitMachinePtr jrpacman_init_machine = new InitMachinePtr() { public void handler() 
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
	
	
		/* check if the loaded set of ROMs allows the Pac Man speed hack */
		if (RAM[0x180b] == 0xbe || RAM[0x180b] == 0x01)
			speedcheat = 1;
		else speedcheat = 0;
	} };
	
	
	
	public static InterruptPtr jrpacman_interrupt = new InterruptPtr() { public int handler() 
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
	
	
		/* speed up cheat */
		if (speedcheat != 0)
		{
			if (readinputport(3) & 1)	/* check status of the fake dip switch */
			{
				/* activate the cheat */
				RAM[0x180b] = 0x01;
			}
			else
			{
				/* remove the cheat */
				RAM[0x180b] = 0xbe;
			}
		}
	
		return interrupt();
	} };
}
