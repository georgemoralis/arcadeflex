/***************************************************************************

Cinematronics sound handlers

Special thanks to Neil Bradley, Zonn Moore, and Jeff Mitchell of the
Retrocade Alliance

Update:
6/27/99 Jim Hernandez -- 1st Attempt at Fixing Drone Star Castle sound and
                         pitch adjustments.
6/30/99 MLR added Rip Off, Solar Quest, Armor Attack (no samples yet)

Bugs: Sometimes the death explosion (small explosion) does not trigger.


***************************************************************************/

#include "driver.h"

static UINT32 current_shift = 0;
static UINT32 last_shift = 0;
static UINT32 last_shift16= 0;
static UINT32 current_pitch = 0x20000;
static UINT32 last_frame = 0;

void cinemat_sound_init (void)
{
    current_shift = 0xffff;
    last_shift = 0xffff;
    last_shift16 = 0xffff;
    current_pitch = 0x20000;
    last_frame = 0;

// Pitch the Drone sound will start off at

}

static void cinemat_shift (UINT8 sound_val, UINT8 bits_changed, UINT8 A1, UINT8 CLK)
{
	// See if we're latching a shift

    if ((bits_changed & CLK) && (0 == (sound_val & CLK)))
	{
		current_shift <<= 1;
		if (sound_val & A1)
            current_shift |= 1;
	}
}

void starcas_sound(UINT8 sound_val, UINT8 bits_changed)
{
    UINT32 target_pitch;
	UINT8 shift_diff;

    cinemat_shift (sound_val, bits_changed, 0x80, 0x10);

	// Now see if it's time to act upon the shifted data

	if ((bits_changed & 0x01) && (0 == (sound_val & 0x01)))
	{
		// Yep. Falling edge! Find out what has changed.

		shift_diff = current_shift ^ last_shift;

		if ((shift_diff & 1) && (0 == (current_shift & 1)))
			sample_start(2, 2, 0);	// Castle fire

		if ((shift_diff & 2) && (0 == (current_shift & 2)))
			sample_start(5, 5, 0);	// Shield hit

		if (shift_diff & 0x04)
		{
			if (current_shift & 0x04)
				sample_start(6, 6, 1);	// Star sound
			else
				sample_stop(6);	// Stop it!
		}

		if (shift_diff & 0x08)
		{
			if (current_shift & 0x08)
				sample_stop(7);	// Stop it!
			else
				sample_start(7, 7, 1);	// Thrust sound
		}

		if (shift_diff & 0x10)
		{
			if (current_shift & 0x10)
				sample_stop(4);
			else
				sample_start(4, 4, 1);	// Drone
		}

		// Latch the drone pitch

        target_pitch = (current_shift & 0x60) >> 3;
        target_pitch |= ((current_shift & 0x40) >> 5);
        target_pitch |= ((current_shift & 0x80) >> 7);

        // target_pitch = (current_shift & 0x60) >> 3;
        // is the the target drone pitch to rise and stop at.

        target_pitch = 0x10000 + (target_pitch << 12);

        // 0x10000 is lowest value the pitch will drop to
        // Star Castle drone sound

        if (cpu_getcurrentframe() > last_frame)
        {
            if (current_pitch > target_pitch)
                current_pitch -= 300;
            if (current_pitch < target_pitch)
                current_pitch += 200;
            sample_set_freq(4, current_pitch);
            last_frame = cpu_getcurrentframe();
        }

		last_shift = current_shift;
	}

	if ((bits_changed & 0x08) && (0 == (sound_val & 0x08)))
		sample_start(3, 3, 0);			// Player fire

	if ((bits_changed & 0x04) && (0 == (sound_val & 0x04)))
		sample_start(1, 1, 0);			// Soft explosion

	if ((bits_changed & 0x02) && (0 == (sound_val & 0x04)))
		sample_start(0, 0, 0);			// Loud explosion

}

void armora_sound(UINT8 sound_val, UINT8 bits_changed)
{
	UINT8 shift_diff;

    cinemat_shift (sound_val, bits_changed, 0x80, 0x10);

	// Now see if it's time to act upon the shifted data

	if ((bits_changed & 0x01) && (0 == (sound_val & 0x01)))
	{
		// Yep. Falling edge! Find out what has changed.

		shift_diff = current_shift ^ last_shift;

		if ((shift_diff & 1) && (0 == (current_shift & 1)))
			sample_start(0, 0, 0);	// Tank fire

		if ((shift_diff & 2) && (0 == (current_shift & 2)))
			sample_start(1, 1, 0);	// Hi explosion

		if ((shift_diff & 4) && (0 == (current_shift & 4)))
			sample_start(2, 2, 0);	// Jeep fire

		if ((shift_diff & 8) && (0 == (current_shift & 8)))
			sample_start(3, 3, 0);	// Lo explosion

        /* High nibble unknown */
		last_shift = current_shift;
	}

    if (bits_changed & 0x2)
    {
        if (sound_val & 0x2)
            sample_start(4, 4, 1);	// Tank +
        else
            sample_stop(4);
    }
    if (bits_changed & 0x4)
    {
        if (sound_val & 0x4)
            sample_start(5, 5, 1);	// Beep +
        else
            sample_stop(5);
    }
    if (bits_changed & 0x8)
    {
        if (sound_val & 0x8)
            sample_start(6, 6, 1);	// Chopper +
        else
            sample_stop(6);
    }
}

void ripoff_sound(UINT8 sound_val, UINT8 bits_changed)
{
	UINT8 shift_diff, current_bg_sound;
    static UINT8 last_bg_sound;

    cinemat_shift (sound_val, bits_changed, 0x01, 0x02);

	// Now see if it's time to act upon the shifted data

	if ((bits_changed & 0x04) && (0 == (sound_val & 0x04)))
	{
		// Yep. Falling edge! Find out what has changed.

		shift_diff = current_shift ^ last_shift;

        current_bg_sound = ((current_shift & 0x1) << 2) | (current_shift & 0x2) | ((current_shift & 0x4) >> 2);
        if (current_bg_sound != last_bg_sound) // use another background sound ?
        {
            shift_diff |= 0x08;
            sample_stop(4);
            last_bg_sound = current_bg_sound;
        }

		if (shift_diff & 0x08)
		{
			if (current_shift & 0x08)
				sample_stop(5);
			else
                sample_start(5, 5+last_bg_sound, 1);	// Background
		}

		if ((shift_diff & 0x10) && (0 == (current_shift & 0x10)))
			sample_start(2, 2, 0);	// Beep

		if (shift_diff & 0x20)
		{
			if (current_shift & 0x20)
				sample_stop(1);	// Stop it!
			else
				sample_start(1, 1, 1);	// Motor
		}

		last_shift = current_shift;
	}

	if ((bits_changed & 0x08) && (0 == (sound_val & 0x08)))
		sample_start(4, 4, 0);			// Torpedo

	if ((bits_changed & 0x10) && (0 == (sound_val & 0x10)))
		sample_start(0, 0, 0);			// Laser

	if ((bits_changed & 0x80) && (0 == (sound_val & 0x80)))
		sample_start(3, 3, 0);			// Explosion

}

void solarq_sound(UINT8 sound_val, UINT8 bits_changed)
{
	UINT32 shift_diff, shift_diff16;
    static int target_volume, current_volume;

    cinemat_shift (sound_val, bits_changed, 0x80, 0x10);

	if ((bits_changed & 0x01) && (0 == (sound_val & 0x01)))
    {
		shift_diff16 = current_shift ^ last_shift16;

		if ((shift_diff16 & 0x1) && (current_shift & 0x1))
        {
            switch (current_shift & 0xffff)
            {
            case 0xceb3:
                sample_start(7, 7, 0);	// Hyperspace
                break;
            case 0x13f3:
                sample_start(7, 8, 0);	// Extra
                break;
            case 0xfdf3:
                sample_start(7, 9, 0);	// Phase
                break;
            case 0x7bf3:
                sample_start(7, 10, 0);	// Enemy fire
                break;
            default:
                if (errorlog)
                    fprintf (errorlog, "Unknown sound starting with: %x\n", current_shift & 0xffff);
                break;
            }
        }

		last_shift16 = current_shift;
    }

	// Now see if it's time to act upon the shifted data

	if ((bits_changed & 0x02) && (0 == (sound_val & 0x02)))
	{
		// Yep. Falling edge! Find out what has changed.

		shift_diff = current_shift ^ last_shift;

		if ((shift_diff & 0x01) && (0 == (current_shift & 0x01)))
			sample_start(0, 0, 0);	// loud expl.

		if ((shift_diff & 0x02) && (0 == (current_shift & 0x02)))
			sample_start(1, 1, 0);	// soft expl.

		if (shift_diff & 0x04) // thrust
		{
			if (current_shift & 0x04)
				target_volume = 0;
			else
            {
                target_volume = 255;
                current_volume = 0;
				sample_start(2, 2, 1);
            }
        }

        if (sample_playing(2) && (last_frame < cpu_getcurrentframe()))
        {
            if (current_volume > target_volume)
                current_volume -= 20;
            if (current_volume < target_volume)
                current_volume += 20;
            if (current_volume > 0)
                sample_set_volume(2, current_volume);
            else
                sample_stop(2);
            last_frame = cpu_getcurrentframe();
        }

		if ((shift_diff & 0x08) && (0 == (current_shift & 0x08)))
			sample_start(3, 3, 0);	// Fire

		if ((shift_diff & 0x10) && (0 == (current_shift & 0x10)))
			sample_start(4, 4, 0);	// Capture

		if (shift_diff & 0x20)
		{
			if (current_shift & 0x20)
				sample_start(6, 6, 1);	// Nuke +
			else
				sample_stop(6);
		}

		if ((shift_diff & 0x40) && (0 == (current_shift & 0x40)))
			sample_start(5, 5, 0);	// Photon

		last_shift = current_shift;
	}
}

void spacewar_sound(UINT8 sound_val, UINT8 bits_changed)
{

	// Explosion

	if (bits_changed & 0x01)
	{
		if (sound_val & 0x01)
		{
            if (rand() & 1)
                sample_start(0, 0, 0);
            else
                sample_start(0, 6, 0);
		}
	}
	// Fire sound

	if ((sound_val & 0x02) && (bits_changed & 0x02))
	{
            if (rand() & 1)
                sample_start(1, 1, 0);
            else
                sample_start(1, 7, 0);
	}

	// Player 1 thrust

	if (bits_changed & 0x04)
	{
		if (sound_val & 0x04)
			sample_stop(3);
		else
			sample_start(3, 3, 1);
	}

	// Player 2 thrust

	if (bits_changed & 0x08)
	{
		if (sound_val & 0x08)
			sample_stop(4);
		else
			sample_start(4, 4, 1);
	}

	// Sound board shutoff (or enable)

	if (bits_changed & 0x10)
	{
		// This is a toggle bit. If sound is enabled, shut everything off.

		if (sound_val & 0x10)
		{
            int i;

			for (i = 0; i < 5; i++)
			{
				if (i != 2)
					sample_stop(i);
			}

			sample_start(2, 5, 0);	// Pop when board is shut off
		}
		else
			sample_start(2, 2, 1);	// Otherwise play idle sound
	}
}

