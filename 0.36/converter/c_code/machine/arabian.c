/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

#include "driver.h"

static UINT8 arabian_clock=0;
static int portB=0;


int arabian_interrupt(void)
{
	arabian_clock++;
	return interrupt();
}


void arabian_portB_w(int offset,int data)
{
	int pc;
	static int last;

	portB = data;

	pc = cpu_get_pc();
	if ((pc == 0x0a7a) || (pc == 0x002a)) pc = cpu_geturnpc();
	if (((data & 0xec) != last) && errorlog)  fprintf(errorlog,"Port B written  %02X  PC=%04X\n",data,pc);
	last = data & 0xec;

	coin_counter_w(0, ~data & 0x01);
	coin_counter_w(1, ~data & 0x02);
}

int arabian_input_port_r(int offset)
{
	int pom;


	if (portB & 0x10)  /* if 1 read the switches */
	{
		switch(offset)
		{
		case 0:  pom = readinputport(2); break;
		case 1:  pom = readinputport(3); break;
		case 2:  pom = readinputport(4); break;
		case 3:  pom = readinputport(5); break;
		case 4:  pom = readinputport(6); break;
		case 5:  pom = readinputport(7); break;
		case 6:  pom = arabian_clock >> 4; break;
		case 8:  pom = arabian_clock & 0x0f; break;
		default:
			if (errorlog)  fprintf(errorlog, "Input Port %04X read.  PC=%04X\n", offset+0xd7f0, cpu_get_pc());
			pom = 0;
			break;
		}
	}
	else  /* if bit 4 of AY port 0f==0 then read RAM memory instead of switches */
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
		pom = RAM[ 0xd7f0 + offset ];
	}

	return pom;
}
