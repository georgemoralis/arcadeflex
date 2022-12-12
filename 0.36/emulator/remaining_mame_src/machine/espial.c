/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

#include "driver.h"
#include "cpu/z80/z80.h"


void espial_init_machine(void)
{
	/* we must start with NMI interrupts disabled */
	//interrupt_enable = 0;
	interrupt_enable_w(0, 0);
}


void zodiac_master_interrupt_enable_w(int offset, int data)
{
	interrupt_enable_w(offset, data ^ 1);
}


int zodiac_master_interrupt(void)
{
	return (cpu_getiloops() == 0) ? nmi_interrupt() : interrupt();
}


void zodiac_master_soundlatch_w(int offset, int data)
{
	soundlatch_w(offset, data);
	cpu_cause_interrupt(1, Z80_IRQ_INT);
}

