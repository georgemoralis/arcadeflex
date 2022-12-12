/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

#include "driver.h"
#include "cpu/m6805/m6805.h"


#define DEBUG_MCU	1


static unsigned char fromz80,toz80;
static int zaccept,zready;

void taitosj_bankswitch_w(int offset,int data);


void taitosj_init_machine(void)
{
	/* set the default ROM bank (many games only have one bank and */
	/* never write to the bank selector register) */
    taitosj_bankswitch_w(0, 0);


	zaccept = 1;
	zready = 0;
	cpu_set_irq_line(2,0,CLEAR_LINE);
}


void taitosj_bankswitch_w(int offset,int data)
{
	unsigned char *RAM = memory_region(REGION_CPU1);

	cpu_setbank(1,&RAM[(data & 0x80) ? 0x10000 : 0x6000]);
}



/***************************************************************************

                           PROTECTION HANDLING

 Some of the games running on this hardware are protected with a 68705 mcu.
 It can either be on a daughter board containing Z80+68705+one ROM, which
 replaces the Z80 on an unprotected main board; or it can be built-in on the
 main board. The two are fucntionally equivalent.

 The 68705 can read commands from the Z80, send back result codes, and has
 direct access to the Z80 memory space. It can also trigger IRQs on the Z80.

***************************************************************************/
int taitosj_fake_data_r(int offset)
{
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: protection read\n",cpu_get_pc());
#endif
	return 0;
}

void taitosj_fake_data_w(int offset,int data)
{
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: protection write %02x\n",cpu_get_pc(),data);
#endif
}

int taitosj_fake_status_r(int offset)
{
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: protection status read\n",cpu_get_pc());
#endif
	return 0xff;
}


/* timer callback : */
void taitosj_mcu_real_data_r(int param)
{
	zaccept = 1;
}

int taitosj_mcu_data_r(int offset)
{
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: protection read %02x\n",cpu_get_pc(),toz80);
#endif
	timer_set(TIME_NOW,0,taitosj_mcu_real_data_r);
	return toz80;
}

/* timer callback : */
void taitosj_mcu_real_data_w(int data)
{
	zready = 1;
	cpu_set_irq_line(2,0,ASSERT_LINE);
	fromz80 = data;
}

void taitosj_mcu_data_w(int offset,int data)
{
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: protection write %02x\n",cpu_get_pc(),data);
#endif
	timer_set(TIME_NOW,data,taitosj_mcu_real_data_w);
}

int taitosj_mcu_status_r(int offset)
{
	/* mcu synchronization */
	cpu_yielduntil_time (TIME_IN_USEC(5));

	/* bit 0 = the 68705 has read data from the Z80 */
	/* bit 1 = the 68705 has written data for the Z80 */
	return ~((zready << 0) | (zaccept << 1));
}

static unsigned char portA_in,portA_out;

int taitosj_68705_portA_r(int offset)
{
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: 68705 port A read %02x\n",cpu_get_pc(),portA_in);
#endif
	return portA_in;
}

void taitosj_68705_portA_w(int offset,int data)
{
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: 68705 port A write %02x\n",cpu_get_pc(),data);
#endif
	portA_out = data;
}



/*
 *  Port B connections:
 *
 *  all bits are logical 1 when read (+5V pullup)
 *
 *  0   W  !68INTRQ
 *  1   W  !68LRD (enables latch which holds command from the Z80)
 *  2   W  !68LWR (loads the latch which holds data for the Z80, and sets a
 *                 status bit so the Z80 knows there's data waiting)
 *  3   W  to Z80 !BUSRQ (aka !WAIT) pin
 *  4   W  !68WRITE (triggers write to main Z80 memory area and increases low
 *                   8 bits of the latched address)
 *  5   W  !68READ (triggers read from main Z80 memory area and increases low
 *                   8 bits of the latched address)
 *  6   W  !LAL (loads the latch which holds the low 8 bits of the address of
 *               the main Z80 memory location to access)
 *  7   W  !UAL (loads the latch which holds the high 8 bits of the address of
 *               the main Z80 memory location to access)
 */

int taitosj_68705_portB_r(int offset)
{
	return 0xff;
}

static int address;

/* timer callback : 68705 is going to read data from the Z80 */
void taitosj_mcu_data_real_r(int param)
{
	zready = 0;
}

/* timer callback : 68705 is writing data for the Z80 */
void taitosj_mcu_status_real_w(int data)
{
	toz80 = data;
	zaccept = 0;
}

void taitosj_68705_portB_w(int offset,int data)
{
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: 68705 port B write %02x\n",cpu_get_pc(),data);
#endif

	if (~data & 0x01)
	{
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: 68705  68INTRQ **NOT SUPPORTED**!\n",cpu_get_pc());
#endif
	}
	if (~data & 0x02)
	{
		/* 68705 is going to read data from the Z80 */
		timer_set(TIME_NOW,0,taitosj_mcu_data_real_r);
		cpu_set_irq_line(2,0,CLEAR_LINE);
		portA_in = fromz80;
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: 68705 <- Z80 %02x\n",cpu_get_pc(),portA_in);
#endif
	}
	if (~data & 0x04)
	{
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: 68705 -> Z80 %02x\n",cpu_get_pc(),portA_out);
#endif
		/* 68705 is writing data for the Z80 */
		timer_set(TIME_NOW,portA_out,taitosj_mcu_status_real_w);
	}
	if (~data & 0x10)
	{
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: 68705 write %02x to address %04x\n",cpu_get_pc(),portA_out,address);
#endif
        memorycontextswap(0);
		cpu_writemem16(address, portA_out);
        memorycontextswap(2);

		/* increase low 8 bits of latched address for burst writes */
		address = (address & 0xff00) | ((address + 1) & 0xff);
	}
	if (~data & 0x20)
	{
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: 68705 read %02x from address %04x\n",cpu_get_pc(),portA_in,address);
#endif
        memorycontextswap(0);
		portA_in = cpu_readmem16(address);
        memorycontextswap(2);
	}
	if (~data & 0x40)
	{
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: 68705 address low %02x\n",cpu_get_pc(),portA_out);
#endif
		address = (address & 0xff00) | portA_out;
	}
	if (~data & 0x80)
	{
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: 68705 address high %02x\n",cpu_get_pc(),portA_out);
#endif
		address = (address & 0x00ff) | (portA_out << 8);
	}
}

/*
 *  Port C connections:
 *
 *  0   R  ZREADY (1 when the Z80 has written a command in the latch)
 *  1   R  ZACCEPT (1 when the Z80 has read data from the latch)
 *  2   R  from Z80 !BUSAK pin
 *  3   R  68INTAK (goes 0 when the interrupt request done with 68INTRQ
 *                  passes through)
 */

int taitosj_68705_portC_r(int offset)
{
	int res;

	res = (zready << 0) | (zaccept << 1);
#if DEBUG_MCU
if (errorlog) fprintf(errorlog,"%04x: 68705 port C read %02x\n",cpu_get_pc(),res);
#endif
	return res;
}



/* Alpine Ski protection crack routines */

static int protection_value;

void alpine_protection_w(int offset, int data)
{
	switch (data)
	{
	case 0x05:
		protection_value = 0x18;
		break;
	case 0x07:
	case 0x0c:
	case 0x0f:
		protection_value = 0x00;		/* not used as far as I can tell */
		break;
	case 0x16:
		protection_value = 0x08;
		break;
	case 0x1d:
		protection_value = 0x18;
		break;
	default:
		protection_value = data;		/* not used as far as I can tell */
		break;
	}
}

void alpinea_bankswitch_w(int offset,int data)
{
    taitosj_bankswitch_w(offset, data);
	protection_value = data >> 2;
}

int alpine_port_2_r(int offset)
{
	return input_port_2_r(offset) | protection_value;
}
