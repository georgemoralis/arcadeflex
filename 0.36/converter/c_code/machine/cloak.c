/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

unsigned char *cloak_sharedram;
unsigned char *cloak_nvRAM;
unsigned char *enable_nvRAM;

int cloak_sharedram_r(int offset)
{
	return cloak_sharedram[offset];
}

void cloak_sharedram_w(int offset,int data)
{
	cloak_sharedram[offset] = data;
}
