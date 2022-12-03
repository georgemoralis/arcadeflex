/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

#include "driver.h"

static int speedcheat = 0;	/* a well known hack allows to make Pac Man run at four times */
					/* his usual speed. When we start the emulation, we check if the */
					/* hack can be applied, and set this flag accordingly. */


void pacman_init_machine(void)
{
	unsigned char *RAM = memory_region(REGION_CPU1);


	/* check if the loaded set of ROMs allows the Pac Man speed hack */
	if ((RAM[0x180b] == 0xbe && RAM[0x1ffd] == 0x00) ||
			(RAM[0x180b] == 0x01 && RAM[0x1ffd] == 0xbd))
		speedcheat = 1;
	else
		speedcheat = 0;
}

int pacman_interrupt(void)
{
	unsigned char *RAM = memory_region(REGION_CPU1);


	/* speed up cheat */
	if (speedcheat)
	{
		if (readinputport(4) & 1)	/* check status of the fake dip switch */
		{
			/* activate the cheat */
			RAM[0x180b] = 0x01;
			RAM[0x1ffd] = 0xbd;
		}
		else
		{
			/* remove the cheat */
			RAM[0x180b] = 0xbe;
			RAM[0x1ffd] = 0x00;
		}
	}

	return interrupt();
}
