/***************************************************************************

sndhrdw\jedi.c

***************************************************************************/

#include "driver.h"

/* Misc sound code */

void jedi_speech_w(int offset, int data)
{
    static unsigned char speech_write_buffer;

    if(offset<0xff)
    {
        speech_write_buffer = data;
    }
    else if (offset<0x1ff)
    {
        tms5220_data_w(0,speech_write_buffer);
    }
}

int jedi_speech_ready_r(int offset)
{
    return (!tms5220_ready_r())<<7;
}
