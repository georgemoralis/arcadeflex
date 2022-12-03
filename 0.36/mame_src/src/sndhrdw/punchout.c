#include "driver.h"

int punchout_input_3_r(int offset)
{
	int data = input_port_3_r(offset);
	/* bit 4 is busy pin level */
	if( VLM5030_BSY() ) data &= ~0x10;
	else data |= 0x10;
	return data;
}

void punchout_speech_reset(int offset,int data)
{
	VLM5030_RST( data&0x01 );
}

void punchout_speech_st(int offset,int data)
{
	VLM5030_ST( data&0x01 );
}

void punchout_speech_vcu(int offset,int data)
{
	VLM5030_VCU( data & 0x01 );
}

