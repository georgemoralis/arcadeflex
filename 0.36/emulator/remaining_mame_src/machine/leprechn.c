/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

#include "driver.h"

static int input_port_select;

void leprechn_input_port_select_w(int offset,int data)
{
    input_port_select = data;
}


int leprechn_input_port_r(int offset)
{
    switch (input_port_select)
    {
    case 0x01:
        return input_port_0_r(0);
    case 0x02:
        return input_port_2_r(0);
    case 0x04:
        return input_port_3_r(0);
    case 0x08:
        return input_port_1_r(0);
    case 0x40:
        return input_port_5_r(0);
    case 0x80:
        return input_port_4_r(0);
    }

    return 0xff;
}


int leprechn_200d_r(int offset)
{
    // Maybe a VSYNC line?
    return 0x02;
}


int leprechn_0805_r(int offset)
{
    return 0xc0;
}
