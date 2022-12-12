/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

#include "driver.h"

/*
 * This wrapper routine is necessary because Millipede requires a direction bit
 * to be set or cleared. The direction bit is held until the mouse is moved
 * again. We still don't understand why the difference between
 * two consecutive reads must not exceed 7. After all, the input is 4 bits
 * wide, and we have a fifth bit for the sign...
 *
 * The other reason it is necessary is that Millipede uses the same address to
 * read the dipswitches.
 */

static int dsw_select;

void milliped_input_select_w(int offset,int data)
{
	dsw_select = (data == 0);
}

int milliped_IN0_r (int offset)
{
	static int oldpos,sign;
	int newpos;

	if (dsw_select)
		return (readinputport(0) | sign);

	newpos = readinputport(6);
	if (newpos != oldpos)
	{
		sign = (newpos - oldpos) & 0x80;
		oldpos = newpos;
	}

	return ((readinputport(0) & 0x70) | (oldpos & 0x0f) | sign );
}

int milliped_IN1_r (int offset)
{
	static int oldpos,sign;
	int newpos;

	if (dsw_select)
		return (readinputport(1) | sign);

	newpos = readinputport(7);
	if (newpos != oldpos)
	{
		sign = (newpos - oldpos) & 0x80;
		oldpos = newpos;
	}

	return ((readinputport(1) & 0x70) | (oldpos & 0x0f) | sign );
}
