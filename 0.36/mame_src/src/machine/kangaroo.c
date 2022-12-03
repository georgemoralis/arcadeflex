/* CHANGELOG
        97/04/xx        renamed the arabian.c and modified it to suit
                        kangaroo. -V-
*/

/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

#include "driver.h"

static int kangaroo_clock=0;


/* I have no idea what the security chip is nor whether it really does,
   this just seems to do the trick -V-
*/

int kangaroo_sec_chip_r(int offset)
{
/*  kangaroo_clock = (kangaroo_clock << 1) + 1; */
  kangaroo_clock++;
  return (kangaroo_clock & 0x0f);
}

void kangaroo_sec_chip_w(int offset, int val)
{
/*  kangaroo_clock = val & 0x0f; */
}
