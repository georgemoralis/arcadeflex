/***************************************************************************

Irem Red Alert sound hardware

The manual lists two sets of sounds.

Analogue:
- Formation Aircraft
- Dive bombers
- Helicopters
- Launcher firing
- Explosion #1
- Explosion #2
- Explosion #3

Digital:
- Melody #1.  Starting sound.
- Melody #2.  Ending sound
- Time signal
- Chirping birds
- Alarm
- Excellent
- Coin insertion
- MIRV division
- Megaton bomb - long
- Megaton bomb - short
- Megaton bomb landing

If you have any questions about how this driver works, don't hesitate to
ask.  - Mike Balfour (mab22@po.cwru.edu)
***************************************************************************/

#include "driver.h"
#include "cpu/m6502/m6502.h"
#include "cpu/i8085/i8085.h"

static int AY8910_A_input_data = 0;
static int c030_data = 0;
static int sound_register_IC1 = 0;
static int sound_register_IC2 = 0;

void redalert_c030_w(int offset, int data)
{
	c030_data = data & 0x3F;

	/* Is this some type of sound command? */
	if (data & 0x80)
		/* Cause an NMI on the voice CPU here? */
		cpu_cause_interrupt(2,I8085_RST75);
}

int redalert_voicecommand_r(int offset)
{
	return c030_data;
}

void redalert_soundlatch_w(int offset, int data)
{
	/* The byte is connected to Port A of the AY8910 */
	AY8910_A_input_data = data;

	/* Bit D7 is also connected to the NMI input of the CPU */
	if ((data & 0x80)!=0x80)
		cpu_cause_interrupt(1,M6502_INT_NMI);
}

int redalert_AY8910_A_r(int offset)
{
	return AY8910_A_input_data;
}

void redalert_AY8910_w(int offset, int data)
{
	/* BC2 is connected to a pull-up resistor, so BC2=1 always */
	switch (data)
	{
		case 0x00:
			/* BC1=0, BDIR=0 : INACTIVE */
			break;
		case 0x01:
			/* BC1=1, BDIR=0 : READ FROM PSG */
			sound_register_IC1 = AY8910_read_port_0_r(offset);
			break;
		case 0x02:
			/* BC1=0, BDIR=1 : WRITE TO PSG */
			AY8910_write_port_0_w(offset,sound_register_IC2);
			break;
		case 0x03:
			/* BC1=1, BDIR=1 : LATCH ADDRESS */
			AY8910_control_port_0_w(offset,sound_register_IC2);
			break;
		default:
			if (errorlog) fprintf(errorlog,"Invalid Sound Command: %02X\n",data);
			break;
	}
}

int redalert_sound_register_IC1_r(int offset)
{
	return sound_register_IC1;
}

void redalert_sound_register_IC2_w(int offset, int data)
{
	sound_register_IC2 = data;
}

void redalert_AY8910_B_w(int offset, int data)
{
	/* I'm fairly certain this port triggers analog sounds */
	if (errorlog) fprintf(errorlog,"Port B Trigger: %02X\n",data);
	/* D0 = Formation Aircraft? */
	/* D1 = Dive bombers? */
	/* D2 = Helicopters? */
	/* D3 = Launcher firing? */
	/* D4 = Explosion #1? */
	/* D5 = Explosion #2? */
	/* D6 = Explosion #3? */
}

