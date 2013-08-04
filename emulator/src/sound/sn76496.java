
package sound;

import mame.sndintrf;
import mame.sndintrfH;
import static mame.sndintrfH.*;
import static sound.sn76496H.*;
import static mame.driverH.*;

public class sn76496 extends sndintrf.snd_interface
{
    public sn76496()
    {
        sound_num=SOUND_SN76496;
        name="SN76496";
    }
    @Override
    public int chips_num(MachineSound msound) {
        return ((SN76496interface)msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((SN76496interface)msound.sound_interface).baseclock[0];
    }

    @Override
    public int start(MachineSound msound) {
       //TODO
        return 0;
    }

    @Override
    public void stop() {
        //no functionality expected
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
       //no functionality expected
    }
    /*TODO*///#define MAX_OUTPUT 0x7fff
/*TODO*///
/*TODO*///#define STEP 0x10000
/*TODO*///
/*TODO*////* noise feedback for white noise mode */
/*TODO*///#define FB_WNOISE 0x12000	/* bit15.d(16bits) = bit0(out) ^ bit2 */
/*TODO*///
/*TODO*///#define FB_PNOISE 0x08000   /* JH 981127 - fixes Do Run Run */
/*TODO*///
/*TODO*////* noise generator start preset (for periodic noise) */
/*TODO*///#define NG_PRESET 0x0f35
/*TODO*///
/*TODO*///
/*TODO*///struct SN76496
/*TODO*///{
/*TODO*///	int Channel;
/*TODO*///	int SampleRate;
/*TODO*///	unsigned int UpdateStep;
/*TODO*///	int VolTable[16];	/* volume table         */
/*TODO*///	int Register[8];	/* registers */
/*TODO*///	int LastRegister;	/* last register written */
/*TODO*///	int Volume[4];		/* volume of voice 0-2 and noise */
/*TODO*///	unsigned int RNG;		/* noise generator      */
/*TODO*///	int NoiseFB;		/* noise feedback mask */
/*TODO*///	int Period[4];
/*TODO*///	int Count[4];
/*TODO*///	int Output[4];
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///static struct SN76496 sn[MAX_76496];
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static void SN76496Write(int chip,int data)
/*TODO*///{
/*TODO*///	struct SN76496 *R = &sn[chip];
/*TODO*///
/*TODO*///
/*TODO*///	/* update the output buffer before changing the registers */
/*TODO*///	stream_update(R->Channel,0);
/*TODO*///
/*TODO*///	if (data & 0x80)
/*TODO*///	{
/*TODO*///		int r = (data & 0x70) >> 4;
/*TODO*///		int c = r/2;
/*TODO*///
/*TODO*///		R->LastRegister = r;
/*TODO*///		R->Register[r] = (R->Register[r] & 0x3f0) | (data & 0x0f);
/*TODO*///		switch (r)
/*TODO*///		{
/*TODO*///			case 0:	/* tone 0 : frequency */
/*TODO*///			case 2:	/* tone 1 : frequency */
/*TODO*///			case 4:	/* tone 2 : frequency */
/*TODO*///				R->Period[c] = R->UpdateStep * R->Register[r];
/*TODO*///				if (R->Period[c] == 0) R->Period[c] = R->UpdateStep;
/*TODO*///				if (r == 4)
/*TODO*///				{
/*TODO*///					/* update noise shift frequency */
/*TODO*///					if ((R->Register[6] & 0x03) == 0x03)
/*TODO*///						R->Period[3] = 2 * R->Period[2];
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 1:	/* tone 0 : volume */
/*TODO*///			case 3:	/* tone 1 : volume */
/*TODO*///			case 5:	/* tone 2 : volume */
/*TODO*///			case 7:	/* noise  : volume */
/*TODO*///				R->Volume[c] = R->VolTable[data & 0x0f];
/*TODO*///				break;
/*TODO*///			case 6:	/* noise  : frequency, mode */
/*TODO*///				{
/*TODO*///					int n = R->Register[6];
/*TODO*///					R->NoiseFB = (n & 4) ? FB_WNOISE : FB_PNOISE;
/*TODO*///					n &= 3;
/*TODO*///					/* N/512,N/1024,N/2048,Tone #3 output */
/*TODO*///					R->Period[3] = (n == 3) ? 2 * R->Period[2] : (R->UpdateStep << (5+n));
/*TODO*///
/*TODO*///					/* reset noise shifter */
/*TODO*///					R->RNG = NG_PRESET;
/*TODO*///					R->Output[3] = R->RNG & 1;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		int r = R->LastRegister;
/*TODO*///		int c = r/2;
/*TODO*///
/*TODO*///		switch (r)
/*TODO*///		{
/*TODO*///			case 0:	/* tone 0 : frequency */
/*TODO*///			case 2:	/* tone 1 : frequency */
/*TODO*///			case 4:	/* tone 2 : frequency */
/*TODO*///				R->Register[r] = (R->Register[r] & 0x0f) | ((data & 0x3f) << 4);
/*TODO*///				R->Period[c] = R->UpdateStep * R->Register[r];
/*TODO*///				if (R->Period[c] == 0) R->Period[c] = R->UpdateStep;
/*TODO*///				if (r == 4)
/*TODO*///				{
/*TODO*///					/* update noise shift frequency */
/*TODO*///					if ((R->Register[6] & 0x03) == 0x03)
/*TODO*///						R->Period[3] = 2 * R->Period[2];
/*TODO*///				}
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
    public static WriteHandlerPtr SN76496_0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
        
    }};
    public static WriteHandlerPtr SN76496_1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
        
    }};
    public static WriteHandlerPtr SN76496_2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
        
    }};
    public static WriteHandlerPtr SN76496_3_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
        
    }};
/*TODO*///void SN76496_0_w(int offset,int data) {	SN76496Write(0,data); }
/*TODO*///void SN76496_1_w(int offset,int data) {	SN76496Write(1,data); }
/*TODO*///void SN76496_2_w(int offset,int data) {	SN76496Write(2,data); }
/*TODO*///void SN76496_3_w(int offset,int data) {	SN76496Write(3,data); }
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static void SN76496Update(int chip,INT16 *buffer,int length)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	struct SN76496 *R = &sn[chip];
/*TODO*///
/*TODO*///
/*TODO*///	/* If the volume is 0, increase the counter */
/*TODO*///	for (i = 0;i < 4;i++)
/*TODO*///	{
/*TODO*///		if (R->Volume[i] == 0)
/*TODO*///		{
/*TODO*///			/* note that I do count += length, NOT count = length + 1. You might think */
/*TODO*///			/* it's the same since the volume is 0, but doing the latter could cause */
/*TODO*///			/* interferencies when the program is rapidly modulating the volume. */
/*TODO*///			if (R->Count[i] <= length*STEP) R->Count[i] += length*STEP;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	while (length > 0)
/*TODO*///	{
/*TODO*///		int vol[4];
/*TODO*///		unsigned int out;
/*TODO*///		int left;
/*TODO*///
/*TODO*///
/*TODO*///		/* vol[] keeps track of how long each square wave stays */
/*TODO*///		/* in the 1 position during the sample period. */
/*TODO*///		vol[0] = vol[1] = vol[2] = vol[3] = 0;
/*TODO*///
/*TODO*///		for (i = 0;i < 3;i++)
/*TODO*///		{
/*TODO*///			if (R->Output[i]) vol[i] += R->Count[i];
/*TODO*///			R->Count[i] -= STEP;
/*TODO*///			/* Period[i] is the half period of the square wave. Here, in each */
/*TODO*///			/* loop I add Period[i] twice, so that at the end of the loop the */
/*TODO*///			/* square wave is in the same status (0 or 1) it was at the start. */
/*TODO*///			/* vol[i] is also incremented by Period[i], since the wave has been 1 */
/*TODO*///			/* exactly half of the time, regardless of the initial position. */
/*TODO*///			/* If we exit the loop in the middle, Output[i] has to be inverted */
/*TODO*///			/* and vol[i] incremented only if the exit status of the square */
/*TODO*///			/* wave is 1. */
/*TODO*///			while (R->Count[i] <= 0)
/*TODO*///			{
/*TODO*///				R->Count[i] += R->Period[i];
/*TODO*///				if (R->Count[i] > 0)
/*TODO*///				{
/*TODO*///					R->Output[i] ^= 1;
/*TODO*///					if (R->Output[i]) vol[i] += R->Period[i];
/*TODO*///					break;
/*TODO*///				}
/*TODO*///				R->Count[i] += R->Period[i];
/*TODO*///				vol[i] += R->Period[i];
/*TODO*///			}
/*TODO*///			if (R->Output[i]) vol[i] -= R->Count[i];
/*TODO*///		}
/*TODO*///
/*TODO*///		left = STEP;
/*TODO*///		do
/*TODO*///		{
/*TODO*///			int nextevent;
/*TODO*///
/*TODO*///
/*TODO*///			if (R->Count[3] < left) nextevent = R->Count[3];
/*TODO*///			else nextevent = left;
/*TODO*///
/*TODO*///			if (R->Output[3]) vol[3] += R->Count[3];
/*TODO*///			R->Count[3] -= nextevent;
/*TODO*///			if (R->Count[3] <= 0)
/*TODO*///			{
/*TODO*///				if (R->RNG & 1) R->RNG ^= R->NoiseFB;
/*TODO*///				R->RNG >>= 1;
/*TODO*///				R->Output[3] = R->RNG & 1;
/*TODO*///				R->Count[3] += R->Period[3];
/*TODO*///				if (R->Output[3]) vol[3] += R->Period[3];
/*TODO*///			}
/*TODO*///			if (R->Output[3]) vol[3] -= R->Count[3];
/*TODO*///
/*TODO*///			left -= nextevent;
/*TODO*///		} while (left > 0);
/*TODO*///
/*TODO*///		out = vol[0] * R->Volume[0] + vol[1] * R->Volume[1] +
/*TODO*///				vol[2] * R->Volume[2] + vol[3] * R->Volume[3];
/*TODO*///
/*TODO*///		if (out > MAX_OUTPUT * STEP) out = MAX_OUTPUT * STEP;
/*TODO*///
/*TODO*///		*(buffer++) = out / STEP;
/*TODO*///
/*TODO*///		length--;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static void SN76496_set_clock(int chip,int clock)
/*TODO*///{
/*TODO*///	struct SN76496 *R = &sn[chip];
/*TODO*///
/*TODO*///
/*TODO*///	/* the base clock for the tone generators is the chip clock divided by 16; */
/*TODO*///	/* for the noise generator, it is clock / 256. */
/*TODO*///	/* Here we calculate the number of steps which happen during one sample */
/*TODO*///	/* at the given sample rate. No. of events = sample rate / (clock/16). */
/*TODO*///	/* STEP is a multiplier used to turn the fraction into a fixed point */
/*TODO*///	/* number. */
/*TODO*///	R->UpdateStep = ((double)STEP * R->SampleRate * 16) / clock;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static void SN76496_set_gain(int chip,int gain)
/*TODO*///{
/*TODO*///	struct SN76496 *R = &sn[chip];
/*TODO*///	int i;
/*TODO*///	double out;
/*TODO*///
/*TODO*///
/*TODO*///	gain &= 0xff;
/*TODO*///
/*TODO*///	/* increase max output basing on gain (0.2 dB per step) */
/*TODO*///	out = MAX_OUTPUT / 3;
/*TODO*///	while (gain-- > 0)
/*TODO*///		out *= 1.023292992;	/* = (10 ^ (0.2/20)) */
/*TODO*///
/*TODO*///	/* build volume table (2dB per step) */
/*TODO*///	for (i = 0;i < 15;i++)
/*TODO*///	{
/*TODO*///		/* limit volume to avoid clipping */
/*TODO*///		if (out > MAX_OUTPUT / 3) R->VolTable[i] = MAX_OUTPUT / 3;
/*TODO*///		else R->VolTable[i] = out;
/*TODO*///
/*TODO*///		out /= 1.258925412;	/* = 10 ^ (2/20) = 2dB */
/*TODO*///	}
/*TODO*///	R->VolTable[15] = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static int SN76496_init(const struct MachineSound *msound,int chip,int clock,int volume,int sample_rate)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	struct SN76496 *R = &sn[chip];
/*TODO*///	char name[40];
/*TODO*///
/*TODO*///
/*TODO*///	sprintf(name,"SN76496 #%d",chip);
/*TODO*///	R->Channel = stream_init(name,volume,sample_rate,chip,SN76496Update);
/*TODO*///
/*TODO*///	if (R->Channel == -1)
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	R->SampleRate = sample_rate;
/*TODO*///	SN76496_set_clock(chip,clock);
/*TODO*///
/*TODO*///	for (i = 0;i < 4;i++) R->Volume[i] = 0;
/*TODO*///
/*TODO*///	R->LastRegister = 0;
/*TODO*///	for (i = 0;i < 8;i+=2)
/*TODO*///	{
/*TODO*///		R->Register[i] = 0;
/*TODO*///		R->Register[i + 1] = 0x0f;	/* volume = 0 */
/*TODO*///	}
/*TODO*///
/*TODO*///	for (i = 0;i < 4;i++)
/*TODO*///	{
/*TODO*///		R->Output[i] = 0;
/*TODO*///		R->Period[i] = R->Count[i] = R->UpdateStep;
/*TODO*///	}
/*TODO*///	R->RNG = NG_PRESET;
/*TODO*///	R->Output[3] = R->RNG & 1;
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///int SN76496_sh_start(const struct MachineSound *msound)
/*TODO*///{
/*TODO*///	int chip;
/*TODO*///	const struct SN76496interface *intf = msound->sound_interface;
/*TODO*///
/*TODO*///
/*TODO*///	for (chip = 0;chip < intf->num;chip++)
/*TODO*///	{
/*TODO*///		if (SN76496_init(msound,chip,intf->baseclock[chip],intf->volume[chip] & 0xff,Machine->sample_rate) != 0)
/*TODO*///			return 1;
/*TODO*///
/*TODO*///		SN76496_set_gain(chip,(intf->volume[chip] >> 8) & 0xff);
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
}
