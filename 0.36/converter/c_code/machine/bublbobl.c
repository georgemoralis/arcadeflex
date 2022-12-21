/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

#include "driver.h"
#include "cpu/z80/z80.h"



unsigned char *bublbobl_sharedram1,*bublbobl_sharedram2;


int bublbobl_sharedram1_r(int offset)
{
	return bublbobl_sharedram1[offset];
}
int bublbobl_sharedram2_r(int offset)
{
	return bublbobl_sharedram2[offset];
}
void bublbobl_sharedram1_w(int offset,int data)
{
	bublbobl_sharedram1[offset] = data;
}
void bublbobl_sharedram2_w(int offset,int data)
{
	bublbobl_sharedram2[offset] = data;
}



void bublbobl_bankswitch_w(int offset,int data)
{
	unsigned char *RAM = memory_region(REGION_CPU1);


	if ((data & 3) == 0) { cpu_setbank(1,&RAM[0x8000]); }
	else { cpu_setbank(1,&RAM[0x10000 + 0x4000 * ((data & 3) - 1)]); }
}

void tokio_bankswitch_w(int offset,int data)
{
	unsigned char *RAM = memory_region(REGION_CPU1);

	cpu_setbank(1, &RAM[0x10000 + 0x4000 * (data & 7)]);
}

void tokio_nmitrigger_w(int offset, int data)
{
	cpu_cause_interrupt(1,Z80_NMI_INT);
}

int tokio_fake_r(int offset)
{
  return 0xbf; /* ad-hoc value set to pass initial testing */
}



static int sound_nmi_enable,pending_nmi;

static void nmi_callback(int param)
{
	if (sound_nmi_enable) cpu_cause_interrupt(2,Z80_NMI_INT);
	else pending_nmi = 1;
}

void bublbobl_sound_command_w(int offset,int data)
{
	soundlatch_w(offset,data);
	timer_set(TIME_NOW,data,nmi_callback);
}

void bublbobl_sh_nmi_disable_w(int offset,int data)
{
	sound_nmi_enable = 0;
}

void bublbobl_sh_nmi_enable_w(int offset,int data)
{
	sound_nmi_enable = 1;
	if (pending_nmi)	/* probably wrong but commands go lost otherwise */
	{
		cpu_cause_interrupt(2,Z80_NMI_INT);
		pending_nmi = 0;
	}
}



/***************************************************************************

 Bubble Bobble 68705 protection interface

 The following is ENTIRELY GUESSWORK!!!

***************************************************************************/
int bublbobl_m68705_interrupt(void)
{
	/* I don't know how to handle the interrupt line so I just toggle it every time. */
	if (cpu_getiloops() & 1)
		cpu_set_irq_line(3,0,CLEAR_LINE);
	else
		cpu_set_irq_line(3,0,ASSERT_LINE);

    return 0;
}



static unsigned char portA_in,portA_out,ddrA;

int bublbobl_68705_portA_r(int offset)
{
//if (errorlog) fprintf(errorlog,"%04x: 68705 port A read %02x\n",cpu_get_pc(),portA_in);
	return (portA_out & ddrA) | (portA_in & ~ddrA);
}

void bublbobl_68705_portA_w(int offset,int data)
{
//if (errorlog) fprintf(errorlog,"%04x: 68705 port A write %02x\n",cpu_get_pc(),data);
	portA_out = data;
}

void bublbobl_68705_ddrA_w(int offset,int data)
{
	ddrA = data;
}



/*
 *  Port B connections:
 *
 *  all bits are logical 1 when read (+5V pullup)
 *
 *  0   W  enables latch which holds data from main Z80 memory
 *  1   W  loads the latch which holds the low 8 bits of the address of
 *               the main Z80 memory location to access
 *  2   W  loads the latch which holds the high 4 bits of the address of
 *               the main Z80 memory location to access
 *         00 = read input ports
 *         0c = access z80 memory at 0xfc00
 *         0f = ????
 *  3   W  selects Z80 memory access direction (0 = write 1 = read)
 *  4   W  clocks main Z80 memory access (goes to a PAL)
 *  5   W  clocks a flip-flop which causes IRQ on the main Z80
 *  6   W  not used?
 *  7   W  not used?
 */

static unsigned char portB_in,portB_out,ddrB;

int bublbobl_68705_portB_r(int offset)
{
	return (portB_out & ddrB) | (portB_in & ~ddrB);
}

static int address,latch;

void bublbobl_68705_portB_w(int offset,int data)
{
//if (errorlog) fprintf(errorlog,"%04x: 68705 port B write %02x\n",cpu_get_pc(),data);

	if ((ddrB & 0x01) && (~data & 0x01) && (portB_out & 0x01))
	{
		portA_in = latch;
	}
	if ((ddrB & 0x02) && (data & 0x02) && (~portB_out & 0x02)) /* positive edge trigger */
	{
		address = (address & 0xff00) | portA_out;
//if (errorlog) fprintf(errorlog,"%04x: 68705 address %02x\n",cpu_get_pc(),portA_out);
	}
	if ((ddrB & 0x04) && (data & 0x04) && (~portB_out & 0x04)) /* positive edge trigger */
	{
		address = (address & 0x00ff) | ((portA_out & 0x0f) << 8);
	}
	if ((ddrB & 0x10) && (~data & 0x10) && (portB_out & 0x10))
	{
		if (data & 0x08)	/* read */
		{
			if ((address & 0x0f00) == 0x0000)
			{
//if (errorlog) fprintf(errorlog,"%04x: 68705 read input port %02x\n",cpu_get_pc(),address);
				latch = readinputport((address & 3) + 1);
			}
			else if ((address & 0x0f00) == 0x0c00)
			{
//if (errorlog) fprintf(errorlog,"%04x: 68705 read %02x from address %04x\n",cpu_get_pc(),bublbobl_sharedram2[address],address);
				latch = bublbobl_sharedram2[address & 0x00ff];
			}
			else
if (errorlog) fprintf(errorlog,"%04x: 68705 unknown read address %04x\n",cpu_get_pc(),address);
		}
		else	/* write */
		{
			if ((address & 0x0f00) == 0x0c00)
			{
//if (errorlog) fprintf(errorlog,"%04x: 68705 write %02x to address %04x\n",cpu_get_pc(),portA_out,address);
				bublbobl_sharedram2[address & 0x00ff] = portA_out;
			}
			else
if (errorlog) fprintf(errorlog,"%04x: 68705 unknown write to address %04x\n",cpu_get_pc(),address);
		}
	}
	if ((ddrB & 0x20) && (~data & 0x20) && (portB_out & 0x20))
	{
		/* hack to get random EXTEND letters (who is supposed to do this? 68705? PAL?) */
		bublbobl_sharedram2[0x7c] = rand()%6;

		cpu_irq_line_vector_w(0,0,bublbobl_sharedram2[0]);
		cpu_set_irq_line(0,0,HOLD_LINE);
	}
	if ((ddrB & 0x40) && (~data & 0x40) && (portB_out & 0x40))
	{
if (errorlog) fprintf(errorlog,"%04x: 68705 unknown port B bit %02x\n",cpu_get_pc(),data);
	}
	if ((ddrB & 0x80) && (~data & 0x80) && (portB_out & 0x80))
	{
if (errorlog) fprintf(errorlog,"%04x: 68705 unknown port B bit %02x\n",cpu_get_pc(),data);
	}

	portB_out = data;
}

void bublbobl_68705_ddrB_w(int offset,int data)
{
	ddrB = data;
}
