#include "driver.h"
#include "cpu/i8039/i8039.h"

unsigned char spacefb_sound_latch;

int  spacefb_sh_getp2(int offset) {
    return ((spacefb_sound_latch & 0x18) << 1);
}

int  spacefb_sh_gett0(int offset)   {
    return spacefb_sound_latch & 0x20;
}

int  spacefb_sh_gett1(int offset)   {
    return spacefb_sound_latch & 0x04;
}

void spacefb_port_1_w(int offset,int data) {
    spacefb_sound_latch = data;
    if (!(data & 0x02)) cpu_cause_interrupt(1,I8039_EXT_INT);
}

void spacefb_sh_putp1(int offset, int data)
{
	DAC_data_w(0,data);
}

