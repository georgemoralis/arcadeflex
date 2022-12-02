/*
 * Aztarac soundboard interface emulation
 *
 * Jul 25 1999 by Mathis Rosenhauer
 *
 */

#include "driver.h"
#include "cpu/z80/z80.h"

static int sound_command, sound_status;

int aztarac_sound_r(int offset)
{
    if (Machine->sample_rate)
        return sound_status & 0x01;
    else
        return 1;
}

void aztarac_sound_w(int offset, int data)
{
    sound_command = data;
    sound_status ^= 0x21;
    if (sound_status & 0x20)
        cpu_cause_interrupt( 1, Z80_IRQ_INT );
}

int aztarac_snd_command_r(int offset)
{
    sound_status |= 0x01;
    sound_status &= ~0x20;
    return sound_command;
}

int aztarac_snd_status_r(int offset)
{
    return sound_status & ~0x01;
}

void aztarac_snd_status_w(int offset, int data)
{
    sound_status &= ~0x10;
}

int aztarac_snd_timed_irq (void)
{
    sound_status ^= 0x10;

    if (sound_status & 0x10)
        return Z80_IRQ_INT;
    else
        return Z80_IGNORE_INT;
}
