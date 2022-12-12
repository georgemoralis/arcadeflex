/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

#include "driver.h"

static unsigned char jedi_control_num = 0;
unsigned char jedi_soundlatch;
unsigned char jedi_soundacklatch;
unsigned char jedi_com_stat;

void jedi_rom_banksel( int offset, int data)
{
	unsigned char *RAM = memory_region(REGION_CPU1);

    if (data & 0x01) cpu_setbank (1, &RAM[0x10000]);
    if (data & 0x02) cpu_setbank (1, &RAM[0x14000]);
    if (data & 0x04) cpu_setbank (1, &RAM[0x18000]);
}

void jedi_sound_reset( int offset, int data)
{
    if (data & 1)
		cpu_set_reset_line(1,CLEAR_LINE);
    else
		cpu_set_reset_line(1,ASSERT_LINE);
}

int jedi_control_r (int offset) {

    if (jedi_control_num == 0)
        return readinputport (2);
    else if (jedi_control_num == 2)
        return readinputport (3);
    return 0;
}

void jedi_control_w (int offset, int data) {

    jedi_control_num = offset;
}


void jedi_soundlatch_w(int offset,int data) {
    jedi_soundlatch = data;
    jedi_com_stat |= 0x80;
}

void jedi_soundacklatch_w(int offset, int data) {
    jedi_soundacklatch = data;
    jedi_com_stat |= 0x40;
}

int jedi_soundlatch_r(int offset) {
    jedi_com_stat &= 0x7F;
    return jedi_soundlatch;
}

int jedi_soundacklatch_r(int offset) {
    jedi_com_stat &= 0xBF;
    return jedi_soundacklatch;
}

int jedi_soundstat_r(int offset) {
    return jedi_com_stat;
}

int jedi_mainstat_r(int offset) {
    unsigned char d;

    d = (jedi_com_stat & 0xC0) >> 1;
    d = d | (input_port_1_r(0) & 0x80);
    d = d | 0x1B;
    return d;
}

