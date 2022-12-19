/**********************************************************************************************
 *
 *   Yamaha YMZ280B driver
 *   by Aaron Giles
 *
 **********************************************************************************************/


#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include "driver.h"
#include "adpcm.h"


#define MAX_SAMPLE_CHUNK	10000

#define FRAC_BITS			14
#define FRAC_ONE			(1 << FRAC_BITS)
#define FRAC_MASK			(FRAC_ONE - 1)


/* struct describing a single playing ADPCM voice */
struct YMZ280BVoice
{
	UINT8 playing;			/* 1 if we are actively playing */

	UINT8 keyon;			/* 1 if the key is on */
	UINT8 looping;			/* 1 if looping is enabled */
	UINT8 mode;				/* current playback mode */
	UINT16 fnum;			/* frequency */
	UINT8 level;			/* output level */
	UINT8 pan;				/* panning */

	UINT32 start;			/* start address, in nibbles */
	UINT32 stop;			/* stop address, in nibbles */
	UINT32 loop_start;		/* loop start address, in nibbles */
	UINT32 loop_end;		/* loop end address, in nibbles */
	UINT32 position;		/* current position, in nibbles */

	INT32 signal;			/* current ADPCM signal */
	UINT8 step;				/* current ADPCM step */

	INT32 output_left;		/* output volume (left) */
	INT32 output_right;		/* output volume (right) */
	INT32 output_step;		/* step value for frequency conversion */
	INT32 output_pos;		/* current fractional position */
	INT16 last_sample;		/* last sample output */
	INT16 curr_sample;		/* current sample target */
};

struct YMZ280BChip
{
	int stream;						/* which stream are we using */
	UINT8 *region_base;				/* pointer to the base of the region */
	UINT8 current_register;			/* currently accessible register */
	UINT8 status_register;			/* current status register */
	UINT8 irq_state;				/* current IRQ state */
	UINT8 irq_mask;					/* current IRQ mask */
	UINT8 irq_enable;				/* current IRQ enable */
	UINT8 keyon_enable;				/* key on enable */
	double master_clock;			/* master clock frequency */
	void (*irq_callback)(int);		/* IRQ callback */
	struct YMZ280BVoice	voice[8];	/* the 8 voices */
};

static struct YMZ280BChip ymz280b[MAX_YMZ280B];
static INT32 *accumulator;
static INT16 *scratch;

/* step size index shift table */
static int index_scale[8] = { 0x0e6, 0x0e6, 0x0e6, 0x0e6, 0x133, 0x199, 0x200, 0x266 };

/* lookup table for the precomputed difference */
static int diff_lookup[16];

/* volume lookup table */
static UINT32 volume_table[256];



INLINE void update_irq_state(struct YMZ280BChip *chip)
{
	int irq_bits = chip->status_register & chip->irq_mask;

	/* always off if the enable is off */
	if (!chip->irq_enable)
		irq_bits = 0;

	/* update the state if changed */
	if (irq_bits && !chip->irq_state)
	{
		chip->irq_state = 1;
		if (chip->irq_callback)
			(*chip->irq_callback)(1);
	}
	else if (!irq_bits && chip->irq_state)
	{
		chip->irq_state = 0;
		if (chip->irq_callback)
			(*chip->irq_callback)(0);
	}
}


INLINE void update_step(struct YMZ280BChip *chip, struct YMZ280BVoice *voice)
{
	double frequency;

	/* handle the sound-off case */
	if (Machine->sample_rate == 0)
	{
		voice->output_step = 0;
		return;
	}

	/* compute the frequency */
	if (voice->mode == 1)
		frequency = chip->master_clock * (double)((voice->fnum & 0x0ff) + 1) * (1.0 / 256.0);
	else
		frequency = chip->master_clock * (double)((voice->fnum & 0x1ff) + 1) * (1.0 / 256.0);
	voice->output_step = (UINT32)(frequency * (double)FRAC_ONE / (double)Machine->sample_rate);
}


INLINE void update_volumes(struct YMZ280BVoice *voice)
{
	if (voice->pan == 8)
	{
		voice->output_left = volume_table[voice->level];
		voice->output_right = volume_table[voice->level];
	}
	else if (voice->pan < 8)
	{
		voice->output_left = volume_table[voice->level];
		voice->output_right = volume_table[voice->level] * voice->pan / 8;
	}
	else
	{
		voice->output_left = volume_table[voice->level] * (15 - voice->pan) / 8;
		voice->output_right = volume_table[voice->level];
	}
}


/**********************************************************************************************

     compute_tables -- compute the difference tables

***********************************************************************************************/

static void compute_tables(void)
{
	int step, nib;

	/* loop over all nibbles and compute the difference */
	for (nib = 0; nib < 16; nib++)
	{
		int value = (nib & 0x07) * 2 + 1;
		diff_lookup[nib] = (nib & 0x08) ? -value : value;
	}

	/* generate the volume table (currently just a guess) */
	for (step = 0; step < 256; step++)
		volume_table[step] = step * 128 / 256;
}



/**********************************************************************************************

     generate_adpcm -- general ADPCM decoding routine

***********************************************************************************************/

static int generate_adpcm(struct YMZ280BVoice *voice, UINT8 *base, INT16 *buffer, int samples)
{
	int position = voice->position;
	int signal = voice->signal;
	int step = voice->step;
	int val;

	/* two cases: first cases is non-looping */
	if (!voice->looping)
	{
		/* loop while we still have samples to generate */
		while (samples)
		{
			/* compute the new amplitude and update the current step */
			val = base[position / 2] >> (((position & 1) << 2) ^ 4);
			signal += (step * diff_lookup[val & 15]) / 8;

			/* clamp to the maximum */
			if (signal > 32767)
				signal = 32767;
			else if (signal < -32768)
				signal = -32768;

			/* adjust the step size and clamp */
			step = (step * index_scale[val & 7]) >> 8;
			if (step > 0x6000)
				step = 0x6000;
			else if (step < 0x7f)
				step = 0x7f;

			/* output to the buffer, scaling by the volume */
			*buffer++ = signal;
			samples--;

			/* next! */
			position++;
			if (position >= voice->stop)
				break;
		}
	}

	/* second case: looping */
	else
	{
		/* loop while we still have samples to generate */
		while (samples)
		{
			/* compute the new amplitude and update the current step */
			val = base[position / 2] >> (((position & 1) << 2) ^ 4);
			signal += (step * diff_lookup[val & 15]) / 8;

			/* clamp to the maximum */
			if (signal > 32767)
				signal = 32767;
			else if (signal < -32768)
				signal = -32768;

			/* adjust the step size and clamp */
			step = (step * index_scale[val & 7]) >> 8;
			if (step > 0x6000)
				step = 0x6000;
			else if (step < 0x7f)
				step = 0x7f;

			/* output to the buffer, scaling by the volume */
			*buffer++ = signal;
			samples--;

			/* next! */
			position++;
			if (position >= voice->loop_end)
			{
				if (voice->keyon)
					position = voice->loop_start;
			}
			if (position >= voice->stop)
				break;
		}
	}

	/* update the parameters */
	voice->position = position;
	voice->signal = signal;
	voice->step = step;

	return samples;
}



/**********************************************************************************************

     generate_pcm8 -- general 8-bit PCM decoding routine

***********************************************************************************************/

static int generate_pcm8(struct YMZ280BVoice *voice, UINT8 *base, INT16 *buffer, int samples)
{
	int position = voice->position;
	int val;

	/* two cases: first cases is non-looping */
	if (!voice->looping)
	{
		/* loop while we still have samples to generate */
		while (samples)
		{
			/* fetch the current value */
			val = base[position / 2];

			/* output to the buffer, scaling by the volume */
			*buffer++ = (INT8)val * 256;
			samples--;

			/* next! */
			position += 2;
			if (position >= voice->stop)
				break;
		}
	}

	/* second case: looping */
	else
	{
		/* loop while we still have samples to generate */
		while (samples)
		{
			/* fetch the current value */
			val = base[position / 2];

			/* output to the buffer, scaling by the volume */
			*buffer++ = (INT8)val * 256;
			samples--;

			/* next! */
			position += 2;
			if (position >= voice->loop_end)
			{
				if (voice->keyon)
					position = voice->loop_start;
			}
			if (position >= voice->stop)
				break;
		}
	}

	/* update the parameters */
	voice->position = position;

	return samples;
}



/**********************************************************************************************

     generate_pcm16 -- general 16-bit PCM decoding routine

***********************************************************************************************/

static int generate_pcm16(struct YMZ280BVoice *voice, UINT8 *base, INT16 *buffer, int samples)
{
	int position = voice->position;
	int val;

	/* two cases: first cases is non-looping */
	if (!voice->looping)
	{
		/* loop while we still have samples to generate */
		while (samples)
		{
			/* fetch the current value */
			val = (INT16)((base[position / 2 + 1] << 8) + base[position / 2]);

			/* output to the buffer, scaling by the volume */
			*buffer++ = val;
			samples--;

			/* next! */
			position += 4;
			if (position >= voice->stop)
				break;
		}
	}

	/* second case: looping */
	else
	{
		/* loop while we still have samples to generate */
		while (samples)
		{
			/* fetch the current value */
			val = (INT16)((base[position / 2 + 1] << 8) + base[position / 2]);

			/* output to the buffer, scaling by the volume */
			*buffer++ = val;
			samples--;

			/* next! */
			position += 4;
			if (position >= voice->loop_end)
			{
				if (voice->keyon)
					position = voice->loop_start;
			}
			if (position >= voice->stop)
				break;
		}
	}

	/* update the parameters */
	voice->position = position;

	return samples;
}



/**********************************************************************************************

     ymz280b_update -- update the sound chip so that it is in sync with CPU execution

***********************************************************************************************/

static void ymz280b_update(int num, INT16 **buffer, int length)
{
	struct YMZ280BChip *chip = &ymz280b[num];
	INT32 *lacc = accumulator;
	INT32 *racc = accumulator + length;
	int v;

	/* clear out the accumulator */
	memset(accumulator, 0, 2 * length * sizeof(accumulator[0]));

	/* loop over voices */
	for (v = 0; v < 8; v++)
	{
		struct YMZ280BVoice *voice = &chip->voice[v];
		INT16 prev = voice->last_sample;
		INT16 curr = voice->curr_sample;
		INT16 *curr_data = scratch;
		INT32 *ldest = lacc;
		INT32 *rdest = racc;
		UINT32 new_samples, samples_left;
		UINT32 final_pos;
		int remaining = length;
		int lvol = voice->output_left;
		int rvol = voice->output_right;

		/* quick out if we're not playing and we're at 0 */
		if (!voice->playing && curr == 0)
			continue;

		/* finish off the current sample */
		if (voice->output_pos > 0)
		{
			/* interpolate */
			while (remaining > 0 && voice->output_pos < FRAC_ONE)
			{
				int interp_sample = (((INT32)prev * (FRAC_ONE - voice->output_pos)) + ((INT32)curr * voice->output_pos)) >> FRAC_BITS;
				*ldest++ += interp_sample * lvol;
				*rdest++ += interp_sample * rvol;
				voice->output_pos += voice->output_step;
				remaining--;
			}

			/* if we're over, continue; otherwise, we're done */
			if (voice->output_pos >= FRAC_ONE)
				voice->output_pos -= FRAC_ONE;
			else
				continue;
		}

		/* compute how many new samples we need */
		final_pos = voice->output_pos + remaining * voice->output_step;
		new_samples = (final_pos + FRAC_ONE - 1) >> FRAC_BITS;
		if (new_samples > MAX_SAMPLE_CHUNK)
			new_samples = MAX_SAMPLE_CHUNK;
		samples_left = new_samples;

		/* generate them into our buffer */
		if (voice->playing)
		{
			switch (voice->mode)
			{
				case 1:	samples_left = generate_adpcm(voice, chip->region_base, scratch, new_samples);	break;
				case 2:	samples_left = generate_pcm8(voice, chip->region_base, scratch, new_samples);	break;
				case 3:	samples_left = generate_pcm16(voice, chip->region_base, scratch, new_samples);	break;
				default:
				case 0:	samples_left = 0; memset(scratch, 0, new_samples * sizeof(scratch[0]));			break;
			}
		}

		/* if there are leftovers, ramp back to 0 */
		if (samples_left)
		{
			int base = new_samples - samples_left;
			int i, t = (base == 0) ? curr : scratch[base - 1];
			for (i = 0; i < samples_left; i++)
			{
				if (t < 0) t = -((-t * 15) >> 4);
				else if (t > 0) t = (t * 15) >> 4;
				scratch[base + i] = t;
			}

			/* if we hit the end and IRQs are enabled, signal it */
			if (base != 0)
			{
				voice->playing = 0;
				chip->status_register |= 1 << v;
				update_irq_state(chip);
			}
		}

		/* advance forward one sample */
		prev = curr;
		curr = *curr_data++;

		/* then sample-rate convert with linear interpolation */
		while (remaining > 0)
		{
			/* interpolate */
			while (remaining > 0 && voice->output_pos < FRAC_ONE)
			{
				int interp_sample = (((INT32)prev * (FRAC_ONE - voice->output_pos)) + ((INT32)curr * voice->output_pos)) >> FRAC_BITS;
				*ldest++ += interp_sample * lvol;
				*rdest++ += interp_sample * rvol;
				voice->output_pos += voice->output_step;
				remaining--;
			}

			/* if we're over, grab the next samples */
			if (voice->output_pos >= FRAC_ONE)
			{
				voice->output_pos -= FRAC_ONE;
				prev = curr;
				curr = *curr_data++;
			}
		}

		/* remember the last samples */
		voice->last_sample = prev;
		voice->curr_sample = curr;
	}

	/* mix and clip the result */
	for (v = 0; v < length; v++)
	{
		int lsamp = lacc[v] / 256;
		int rsamp = racc[v] / 256;

		if (lsamp < -32768) lsamp = -32768;
		else if (lsamp > 32767) lsamp = 32767;
		if (rsamp < -32768) rsamp = -32768;
		else if (rsamp > 32767) rsamp = 32767;

		buffer[0][v] = lsamp;
		buffer[1][v] = rsamp;
	}
}



/**********************************************************************************************

     YMZ280B_sh_start -- start emulation of the YMZ280B

***********************************************************************************************/

int YMZ280B_sh_start(const struct MachineSound *msound)
{
	const struct YMZ280Binterface *intf = msound->sound_interface;
	char stream_name[2][40];
	const char *stream_name_ptrs[2];
	int vol[2];
	int i;

	/* compute ADPCM tables */
	compute_tables();

	/* initialize the voices */
	memset(&ymz280b, 0, sizeof(ymz280b));
	for (i = 0; i < intf->num; i++)
	{
		/* generate the name and create the stream */
		sprintf(stream_name[0], "%s #%d (Left)", sound_name(msound), i);
		sprintf(stream_name[1], "%s #%d (Right)", sound_name(msound), i);
		stream_name_ptrs[0] = stream_name[0];
		stream_name_ptrs[1] = stream_name[1];

		/* set the volumes */
		vol[0] = MIXER(intf->mixing_level[i], MIXER_PAN_LEFT);
		vol[1] = MIXER(intf->mixing_level[i], MIXER_PAN_RIGHT);

		/* create the stream */
		ymz280b[i].stream = stream_init_multi(2, stream_name_ptrs, vol, Machine->sample_rate, i, ymz280b_update);
		if (ymz280b[i].stream == -1)
			return 1;

		/* initialize the rest of the structure */
		ymz280b[i].master_clock = (double)intf->baseclock[i] / 384.0;
		ymz280b[i].region_base = memory_region(intf->region[i]);
		ymz280b[i].irq_callback = intf->irq_callback[i];
	}

	/* allocate memory */
	accumulator = malloc(sizeof(accumulator[0]) * 2 * MAX_SAMPLE_CHUNK);
	scratch = malloc(sizeof(scratch[0]) * MAX_SAMPLE_CHUNK);
	if (!accumulator || !scratch)
		return 1;

	/* success */
	return 0;
}



/**********************************************************************************************

     YMZ280B_sh_stop -- stop emulation of the YMZ280B

***********************************************************************************************/

void YMZ280B_sh_stop(void)
{
	/* free memory */
	if (accumulator)
		free(accumulator);
	accumulator = NULL;

	if (scratch)
		free(scratch);
	scratch = NULL;
}



/**********************************************************************************************

     write_to_register -- handle a write to the current register

***********************************************************************************************/

static void write_to_register(struct YMZ280BChip *chip, int data)
{
	struct YMZ280BVoice *voice;
	int i;

	/* force an update */
	stream_update(chip->stream, 0);

	/* lower registers follow a pattern */
	if (chip->current_register < 0x80)
	{
		voice = &chip->voice[(chip->current_register >> 2) & 7];

		switch (chip->current_register & 0xe3)
		{
			case 0x00:		/* pitch low 8 bits */
				voice->fnum = (voice->fnum & 0x100) | (data & 0xff);
				update_step(chip, voice);
				break;

			case 0x01:		/* pitch upper 1 bit, loop, key on, mode */
				voice->fnum = (voice->fnum & 0xff) | ((data & 0x01) << 8);
				voice->looping = (data & 0x10) >> 4;
				voice->mode = (data & 0x60) >> 5;
				if (!voice->keyon && (data & 0x80) && chip->keyon_enable)
				{
					voice->playing = 1;
					voice->position = voice->start;
					voice->signal = 0;
					voice->step = 0x7f;
				}
				if (voice->keyon && !(data & 0x80) && !voice->looping)
					voice->playing = 0;
				voice->keyon = (data & 0x80) >> 7;
				update_step(chip, voice);
				break;

			case 0x02:		/* total level */
				voice->level = data;
				update_volumes(voice);
				break;

			case 0x03:		/* pan */
				voice->pan = data & 0x0f;
				update_volumes(voice);
				break;

			case 0x20:		/* start address high */
				voice->start = (voice->start & (0x00ffff << 1)) | (data << 17);
				break;

			case 0x21:		/* loop start address high */
				voice->loop_start = (voice->loop_start & (0x00ffff << 1)) | (data << 17);
				break;

			case 0x22:		/* loop end address high */
				voice->loop_end = (voice->loop_end & (0x00ffff << 1)) | (data << 17);
				break;

			case 0x23:		/* stop address high */
				voice->stop = (voice->stop & (0x00ffff << 1)) | (data << 17);
				break;

			case 0x40:		/* start address middle */
				voice->start = (voice->start & (0xff00ff << 1)) | (data << 9);
				break;

			case 0x41:		/* loop start address middle */
				voice->loop_start = (voice->loop_start & (0xff00ff << 1)) | (data << 9);
				break;

			case 0x42:		/* loop end address middle */
				voice->loop_end = (voice->loop_end & (0xff00ff << 1)) | (data << 9);
				break;

			case 0x43:		/* stop address middle */
				voice->stop = (voice->stop & (0xff00ff << 1)) | (data << 9);
				break;

			case 0x60:		/* start address low */
				voice->start = (voice->start & (0xffff00 << 1)) | (data << 1);
				break;

			case 0x61:		/* loop start address low */
				voice->loop_start = (voice->loop_start & (0xffff00 << 1)) | (data << 1);
				break;

			case 0x62:		/* loop end address low */
				voice->loop_end = (voice->loop_end & (0xffff00 << 1)) | (data << 1);
				break;

			case 0x63:		/* stop address low */
				voice->stop = (voice->stop & (0xffff00 << 1)) | (data << 1);
				break;

			default:
				logerror("YMZ280B: unknown register write %02X = %02X\n", chip->current_register, data);
				break;
		}
	}

	/* upper registers are special */
	else
	{
		switch (chip->current_register)
		{
			case 0xfe:		/* IRQ mask */
				chip->irq_mask = data;
				update_irq_state(chip);
				break;

			case 0xff:		/* IRQ enable, test, etc */
				chip->irq_enable = (data & 0x10) >> 4;
				update_irq_state(chip);
				chip->keyon_enable = (data & 0x80) >> 7;
				if (!chip->keyon_enable)
					for (i = 0; i < 8; i++)
						chip->voice[i].playing = 0;
				break;

			default:
				logerror("YMZ280B: unknown register write %02X = %02X\n", chip->current_register, data);
				break;
		}
	}
}



/**********************************************************************************************

     compute_status -- determine the status bits

***********************************************************************************************/

static int compute_status(struct YMZ280BChip *chip)
{
	UINT8 result = chip->status_register;

	/* force an update */
	stream_update(chip->stream, 0);

	/* clear the IRQ state */
	chip->status_register = 0;
	update_irq_state(chip);

	return result;
}



/**********************************************************************************************

     YMZ280B_status_0_r/YMZ280B_status_1_r -- handle a read from the status register

***********************************************************************************************/

READ_HANDLER( YMZ280B_status_0_r )
{
	return compute_status(&ymz280b[0]);
}

READ_HANDLER( YMZ280B_status_1_r )
{
	return compute_status(&ymz280b[1]);
}



/**********************************************************************************************

     YMZ280B_register_0_w/YMZ280B_register_1_w -- handle a write to the register select

***********************************************************************************************/

WRITE_HANDLER( YMZ280B_register_0_w )
{
	ymz280b[0].current_register = data;
}

WRITE_HANDLER( YMZ280B_register_1_w )
{
	ymz280b[1].current_register = data;
}



/**********************************************************************************************

     YMZ280B_data_0_w/YMZ280B_data_1_w -- handle a write to the current register

***********************************************************************************************/

WRITE_HANDLER( YMZ280B_data_0_w )
{
	write_to_register(&ymz280b[0], data);
}

WRITE_HANDLER( YMZ280B_data_1_w )
{
	write_to_register(&ymz280b[1], data);
}
