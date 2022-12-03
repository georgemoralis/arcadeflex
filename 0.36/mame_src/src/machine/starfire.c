/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

#include "driver.h"

static int sound_ctrl;
static int fireone_sell;

/* In drivers/starfire.c */
extern unsigned char *starfire_ram;

/* In vidhrdw/starfire.c */
extern void starfire_vidctrl_w(int offset,int data);
extern void starfire_vidctrl1_w(int offset,int data);

void starfire_shadow_w(int address, int data)
{
    starfire_ram[address & 0x3ff] = data;
}

void starfire_output_w(int address, int data)
{
    starfire_ram[address & 0x3ff] = data;
    switch(address & 0xf) {
    case 0:
		starfire_vidctrl_w(0, data);
		break;
    case 1:
		starfire_vidctrl1_w(0, data);
		break;
    case 2:
		/* Sounds */
		break;
    }
}

void fireone_output_w(int address, int data)
{
    starfire_ram[address & 0x3ff] = data;
    switch(address & 0xf) {
    case 0:
		starfire_vidctrl_w(0, data);
		break;
    case 1:
		starfire_vidctrl1_w(0, data);
		break;
    case 2:
		/* Sounds */
		fireone_sell = (data & 0x8) ? 0 : 1;
		break;
    }
}

int starfire_shadow_r(int address)
{
    return starfire_ram[address & 0x3ff];
}

int starfire_input_r(int address)
{
    switch(address & 0xf) {
    case 0:
		return input_port_0_r(0);
    case 1:
		/* Note : need to loopback sounds lengths on that one */
		return input_port_1_r(0);
    case 5:
		/* Throttle, should be analog too */
		return input_port_4_r(0);
    case 6:
		return input_port_2_r(0);
    case 7:
		return input_port_3_r(0);
    default:
		return 0xff;
    }
}

int fireone_input_r(int address)
{
    switch(address & 0xf) {
    case 0:
		return input_port_0_r(0);
    case 1:
		return input_port_1_r(0);
    case 2:
		/* Throttle, should be analog too */
		return fireone_sell ? input_port_2_r(0) : input_port_3_r(0);
    default:
		return 0xff;
    }
}

void starfire_soundctrl_w(int offset, int data) {
    sound_ctrl = data;
}

int starfire_io1_r(int offset) {
    int in,out;

    in = readinputport(1);
    out = (in & 0x07) | 0xE0;

    if (sound_ctrl & 0x04)
        out = out | 0x08;
    else
        out = out & 0xF7;

    if (sound_ctrl & 0x08)
        out = out | 0x10;
    else
        out = out & 0xEF;

    return out;
}

int starfire_interrupt (void)
{

    return nmi_interrupt();
}
