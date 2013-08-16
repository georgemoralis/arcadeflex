/***************************************************************************

  ay8910.c


  Emulation of the AY-3-8910 / YM2149 sound chip.

  Based on various code snippets by Ville Hallik, Michael Cuddy,
  Tatsuyuki Satoh, Fabrice Frances, Nicola Salmoria.

***************************************************************************/
package sound;
import mame.sndintrf;
import mame.sndintrfH;
import static mame.sndintrfH.*;
import static sound.ay8910H.*;
import static mame.driverH.*;
import static mame.mame.*;
import static arcadeflex.libc_old.*;
import static mame.cpuintrf.*;

public class ay8910 extends sndintrf.snd_interface
{
    public ay8910()
    {
        sound_num=SOUND_AY8910;
        name="AY-8910";
        for (int i = 0; i < MAX_8910; i++) AYPSG[i] = new AY8910();
    }
    @Override
    public int chips_num(MachineSound msound) {
        return ((AY8910interface)msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((AY8910interface)msound.sound_interface).baseclock;
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
    
    public static final int MAX_OUTPUT = 0x7fff;
    
    public static final int STEP = 0x8000;
    
    public static class AY8910
    {
        int Channel;
    	int SampleRate;
    	ReadHandlerPtr PortAread;//int (*PortAread)(int offset);
    	ReadHandlerPtr PortBread;//int (*PortBread)(int offset);
    	WriteHandlerPtr PortAwrite;//void (*PortAwrite)(int offset,int data);
    	WriteHandlerPtr PortBwrite;//void (*PortBwrite)(int offset,int data);
    	int register_latch;
    	/*unsigned*/ char Regs[]=new char[16];
    	/*unsigned*/ int UpdateStep;
    	int PeriodA,PeriodB,PeriodC,PeriodN,PeriodE;
    	int CountA,CountB,CountC,CountN,CountE;
    	/*unsigned*/ int VolA,VolB,VolC,VolE;
    	/*unsigned*/ char EnvelopeA,EnvelopeB,EnvelopeC;
    	/*unsigned*/ char OutputA,OutputB,OutputC,OutputN;
    	char CountEnv;
    	/*unsigned*/ char Hold,Alternate,Attack,Holding;
    	int RNG;
    	/*unsigned*/ int VolTable[] = new int[32];
    }

    /*TODO*///
    /*TODO*////* register id's */
    /*TODO*///#define AY_AFINE	(0)
    /*TODO*///#define AY_ACOARSE	(1)
    /*TODO*///#define AY_BFINE	(2)
    /*TODO*///#define AY_BCOARSE	(3)
    /*TODO*///#define AY_CFINE	(4)
    /*TODO*///#define AY_CCOARSE	(5)
    /*TODO*///#define AY_NOISEPER	(6)
    public static final int AY_ENABLE	= 7;
    /*TODO*///#define AY_AVOL		(8)
    /*TODO*///#define AY_BVOL		(9)
    /*TODO*///#define AY_CVOL		(10)
    /*TODO*///#define AY_EFINE	(11)
    /*TODO*///#define AY_ECOARSE	(12)
    /*TODO*///#define AY_ESHAPE	(13)
    
    public static final int AY_PORTA =	14;
    public static final int AY_PORTB =	15;

    
    static AY8910[] AYPSG = new AY8910[MAX_8910]; /* array of PSG's */
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///void _AYWriteReg(int n, int r, int v)
    /*TODO*///{
    /*TODO*///	struct AY8910 *PSG = &AYPSG[n];
    /*TODO*///	int old;
    /*TODO*///
    /*TODO*///
    /*TODO*///	PSG->Regs[r] = v;
    /*TODO*///
    /*TODO*///	/* A note about the period of tones, noise and envelope: for speed reasons,*/
    /*TODO*///	/* we count down from the period to 0, but careful studies of the chip     */
    /*TODO*///	/* output prove that it instead counts up from 0 until the counter becomes */
    /*TODO*///	/* greater or equal to the period. This is an important difference when the*/
    /*TODO*///	/* program is rapidly changing the period to modulate the sound.           */
    /*TODO*///	/* To compensate for the difference, when the period is changed we adjust  */
    /*TODO*///	/* our internal counter.                                                   */
    /*TODO*///	/* Also, note that period = 0 is the same as period = 1. This is mentioned */
    /*TODO*///	/* in the YM2203 data sheets. However, this does NOT apply to the Envelope */
    /*TODO*///	/* period. In that case, period = 0 is half as period = 1. */
    /*TODO*///	switch( r )
    /*TODO*///	{
    /*TODO*///	case AY_AFINE:
    /*TODO*///	case AY_ACOARSE:
    /*TODO*///		PSG->Regs[AY_ACOARSE] &= 0x0f;
    /*TODO*///		old = PSG->PeriodA;
    /*TODO*///		PSG->PeriodA = (PSG->Regs[AY_AFINE] + 256 * PSG->Regs[AY_ACOARSE]) * PSG->UpdateStep;
    /*TODO*///		if (PSG->PeriodA == 0) PSG->PeriodA = PSG->UpdateStep;
    /*TODO*///		PSG->CountA += PSG->PeriodA - old;
    /*TODO*///		if (PSG->CountA <= 0) PSG->CountA = 1;
    /*TODO*///		break;
    /*TODO*///	case AY_BFINE:
    /*TODO*///	case AY_BCOARSE:
    /*TODO*///		PSG->Regs[AY_BCOARSE] &= 0x0f;
    /*TODO*///		old = PSG->PeriodB;
    /*TODO*///		PSG->PeriodB = (PSG->Regs[AY_BFINE] + 256 * PSG->Regs[AY_BCOARSE]) * PSG->UpdateStep;
    /*TODO*///		if (PSG->PeriodB == 0) PSG->PeriodB = PSG->UpdateStep;
    /*TODO*///		PSG->CountB += PSG->PeriodB - old;
    /*TODO*///		if (PSG->CountB <= 0) PSG->CountB = 1;
    /*TODO*///		break;
    /*TODO*///	case AY_CFINE:
    /*TODO*///	case AY_CCOARSE:
    /*TODO*///		PSG->Regs[AY_CCOARSE] &= 0x0f;
    /*TODO*///		old = PSG->PeriodC;
    /*TODO*///		PSG->PeriodC = (PSG->Regs[AY_CFINE] + 256 * PSG->Regs[AY_CCOARSE]) * PSG->UpdateStep;
    /*TODO*///		if (PSG->PeriodC == 0) PSG->PeriodC = PSG->UpdateStep;
    /*TODO*///		PSG->CountC += PSG->PeriodC - old;
    /*TODO*///		if (PSG->CountC <= 0) PSG->CountC = 1;
    /*TODO*///		break;
    /*TODO*///	case AY_NOISEPER:
    /*TODO*///		PSG->Regs[AY_NOISEPER] &= 0x1f;
    /*TODO*///		old = PSG->PeriodN;
    /*TODO*///		PSG->PeriodN = PSG->Regs[AY_NOISEPER] * PSG->UpdateStep;
    /*TODO*///		if (PSG->PeriodN == 0) PSG->PeriodN = PSG->UpdateStep;
    /*TODO*///		PSG->CountN += PSG->PeriodN - old;
    /*TODO*///		if (PSG->CountN <= 0) PSG->CountN = 1;
    /*TODO*///		break;
    /*TODO*///	case AY_AVOL:
    /*TODO*///		PSG->Regs[AY_AVOL] &= 0x1f;
    /*TODO*///		PSG->EnvelopeA = PSG->Regs[AY_AVOL] & 0x10;
    /*TODO*///		PSG->VolA = PSG->EnvelopeA ? PSG->VolE : PSG->VolTable[PSG->Regs[AY_AVOL] ? PSG->Regs[AY_AVOL]*2+1 : 0];
    /*TODO*///		break;
    /*TODO*///	case AY_BVOL:
    /*TODO*///		PSG->Regs[AY_BVOL] &= 0x1f;
    /*TODO*///		PSG->EnvelopeB = PSG->Regs[AY_BVOL] & 0x10;
    /*TODO*///		PSG->VolB = PSG->EnvelopeB ? PSG->VolE : PSG->VolTable[PSG->Regs[AY_BVOL] ? PSG->Regs[AY_BVOL]*2+1 : 0];
    /*TODO*///		break;
    /*TODO*///	case AY_CVOL:
    /*TODO*///		PSG->Regs[AY_CVOL] &= 0x1f;
    /*TODO*///		PSG->EnvelopeC = PSG->Regs[AY_CVOL] & 0x10;
    /*TODO*///		PSG->VolC = PSG->EnvelopeC ? PSG->VolE : PSG->VolTable[PSG->Regs[AY_CVOL] ? PSG->Regs[AY_CVOL]*2+1 : 0];
    /*TODO*///		break;
    /*TODO*///	case AY_EFINE:
    /*TODO*///	case AY_ECOARSE:
    /*TODO*///		old = PSG->PeriodE;
    /*TODO*///		PSG->PeriodE = ((PSG->Regs[AY_EFINE] + 256 * PSG->Regs[AY_ECOARSE])) * PSG->UpdateStep;
    /*TODO*///		if (PSG->PeriodE == 0) PSG->PeriodE = PSG->UpdateStep / 2;
    /*TODO*///		PSG->CountE += PSG->PeriodE - old;
    /*TODO*///		if (PSG->CountE <= 0) PSG->CountE = 1;
    /*TODO*///		break;
    /*TODO*///	case AY_ESHAPE:
    /*TODO*///		/* envelope shapes:
    /*TODO*///		C AtAlH
    /*TODO*///		0 0 x x  \___
    /*TODO*///
    /*TODO*///		0 1 x x  /___
    /*TODO*///
    /*TODO*///		1 0 0 0  \\\\
    /*TODO*///
    /*TODO*///		1 0 0 1  \___
    /*TODO*///
    /*TODO*///		1 0 1 0  \/\/
    /*TODO*///		          ___
    /*TODO*///		1 0 1 1  \
    /*TODO*///
    /*TODO*///		1 1 0 0  ////
    /*TODO*///		          ___
    /*TODO*///		1 1 0 1  /
    /*TODO*///
    /*TODO*///		1 1 1 0  /\/\
    /*TODO*///
    /*TODO*///		1 1 1 1  /___
    /*TODO*///
    /*TODO*///		The envelope counter on the AY-3-8910 has 16 steps. On the YM2149 it
    /*TODO*///		has twice the steps, happening twice as fast. Since the end result is
    /*TODO*///		just a smoother curve, we always use the YM2149 behaviour.
    /*TODO*///		*/
    /*TODO*///		PSG->Regs[AY_ESHAPE] &= 0x0f;
    /*TODO*///		PSG->Attack = (PSG->Regs[AY_ESHAPE] & 0x04) ? 0x1f : 0x00;
    /*TODO*///		if ((PSG->Regs[AY_ESHAPE] & 0x08) == 0)
    /*TODO*///		{
    /*TODO*///			/* if Continue = 0, map the shape to the equivalent one which has Continue = 1 */
    /*TODO*///			PSG->Hold = 1;
    /*TODO*///			PSG->Alternate = PSG->Attack;
    /*TODO*///		}
    /*TODO*///		else
    /*TODO*///		{
    /*TODO*///			PSG->Hold = PSG->Regs[AY_ESHAPE] & 0x01;
    /*TODO*///			PSG->Alternate = PSG->Regs[AY_ESHAPE] & 0x02;
    /*TODO*///		}
    /*TODO*///		PSG->CountE = PSG->PeriodE;
    /*TODO*///		PSG->CountEnv = 0x1f;
    /*TODO*///		PSG->Holding = 0;
    /*TODO*///		PSG->VolE = PSG->VolTable[PSG->CountEnv ^ PSG->Attack];
    /*TODO*///		if (PSG->EnvelopeA) PSG->VolA = PSG->VolE;
    /*TODO*///		if (PSG->EnvelopeB) PSG->VolB = PSG->VolE;
    /*TODO*///		if (PSG->EnvelopeC) PSG->VolC = PSG->VolE;
    /*TODO*///		break;
    /*TODO*///	case AY_PORTA:
    /*TODO*///		if ((PSG->Regs[AY_ENABLE] & 0x40) == 0)
    /*TODO*///if (errorlog) fprintf(errorlog,"warning: write to 8910 #%d Port A set as input\n",n);
    /*TODO*///if (PSG->PortAwrite) (*PSG->PortAwrite)(0,v);
    /*TODO*///else if (errorlog) fprintf(errorlog,"PC %04x: warning - write %02x to 8910 #%d Port A\n",cpu_get_pc(),v,n);
    /*TODO*///		break;
    /*TODO*///	case AY_PORTB:
    /*TODO*///		if ((PSG->Regs[AY_ENABLE] & 0x80) == 0)
    /*TODO*///if (errorlog) fprintf(errorlog,"warning: write to 8910 #%d Port B set as input\n",n);
    /*TODO*///if (PSG->PortBwrite) (*PSG->PortBwrite)(0,v);
    /*TODO*///else if (errorlog) fprintf(errorlog,"PC %04x: warning - write %02x to 8910 #%d Port B\n",cpu_get_pc(),v,n);
    /*TODO*///		break;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////* write a register on AY8910 chip number 'n' */
    /*TODO*///void AYWriteReg(int chip, int r, int v)
    /*TODO*///{
    /*TODO*///	struct AY8910 *PSG = &AYPSG[chip];
    /*TODO*///
    /*TODO*///
    /*TODO*///	if (r > 15) return;
    /*TODO*///	if (r < 14)
    /*TODO*///	{
    /*TODO*///		if (r == AY_ESHAPE || PSG->Regs[r] != v)
    /*TODO*///		{
    /*TODO*///			/* update the output buffer before changing the register */
    /*TODO*///			stream_update(PSG->Channel,0);
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	_AYWriteReg(chip,r,v);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    
    static /*unsigned*/ char AYReadReg(int n, int r)
    {
    	AY8910 PSG = AYPSG[n];
    
    
    	if (r > 15) return 0;
    
    	switch (r)
    	{
    	case AY_PORTA:
    		if ((PSG.Regs[AY_ENABLE] & 0x40) != 0)
                    if (errorlog!=null) fprintf(errorlog,"warning: read from 8910 #%d Port A set as output\n",n);
                    if (PSG.PortAread!=null) PSG.Regs[AY_PORTA] = (char)(PSG.PortAread.handler(0) & 0xFF);//(*PSG->PortAread)(0);
                    else if (errorlog!=null) fprintf(errorlog,"PC %04x: warning - read 8910 #%d Port A\n",cpu_get_pc(),n);
    		break;
    	case AY_PORTB:
    		if ((PSG.Regs[AY_ENABLE] & 0x80) != 0)
                if (errorlog!=null) fprintf(errorlog,"warning: read from 8910 #%d Port B set as output\n",n);
                if (PSG.PortBread!=null) PSG.Regs[AY_PORTB] = (char)(PSG.PortBread.handler(0) & 0xFF);
                else if (errorlog!=null) fprintf(errorlog,"PC %04x: warning - read 8910 #%d Port B\n",cpu_get_pc(),n);
    		break;
    	}
    	return (char)(PSG.Regs[r] & 0xFF);
    }
    
    
    public static void AY8910Write(int chip,int a,int data)
    {
    	AY8910 PSG = AYPSG[chip];
    
    	if ((a & 1)!=0)
    	{	/* Data port */
    /*TODO*///		AYWriteReg(chip,PSG->register_latch,data);
    	}
    	else
    	{	/* Register port */
    		PSG.register_latch = data & 0x0f;
    	}
    }
    
    static int AY8910Read(int chip)
    {
    	AY8910 PSG = AYPSG[chip];
    
    	return AYReadReg(chip,PSG.register_latch);
    }
    
    
    /* AY8910 interface */
    public static ReadHandlerPtr AY8910_read_port_0_r = new ReadHandlerPtr() { public int handler(int offset)
    {
        return AY8910Read(0);
    }};
    /*TODO*///int AY8910_read_port_1_r(int offset) { return AY8910Read(1); }
    /*TODO*///int AY8910_read_port_2_r(int offset) { return AY8910Read(2); }
    /*TODO*///int AY8910_read_port_3_r(int offset) { return AY8910Read(3); }
    /*TODO*///int AY8910_read_port_4_r(int offset) { return AY8910Read(4); }
    /*TODO*///
    public static WriteHandlerPtr AY8910_control_port_0_w = new WriteHandlerPtr() {	public void handler(int offset, int data)
    {
        AY8910Write(0,0,data);
    }};
    /*TODO*///void AY8910_control_port_0_w(int offset,int data) { AY8910Write(0,0,data); }
    /*TODO*///void AY8910_control_port_1_w(int offset,int data) { AY8910Write(1,0,data); }
    /*TODO*///void AY8910_control_port_2_w(int offset,int data) { AY8910Write(2,0,data); }
    /*TODO*///void AY8910_control_port_3_w(int offset,int data) { AY8910Write(3,0,data); }
    /*TODO*///void AY8910_control_port_4_w(int offset,int data) { AY8910Write(4,0,data); }
    /*TODO*///
    public static WriteHandlerPtr AY8910_write_port_0_w = new WriteHandlerPtr() {	public void handler(int offset, int data)
    {
        AY8910Write(0,1,data);
    }};
    /*TODO*///void AY8910_write_port_0_w(int offset,int data) { AY8910Write(0,1,data); }
    /*TODO*///void AY8910_write_port_1_w(int offset,int data) { AY8910Write(1,1,data); }
    /*TODO*///void AY8910_write_port_2_w(int offset,int data) { AY8910Write(2,1,data); }
    /*TODO*///void AY8910_write_port_3_w(int offset,int data) { AY8910Write(3,1,data); }
    /*TODO*///void AY8910_write_port_4_w(int offset,int data) { AY8910Write(4,1,data); }
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///static void AY8910Update(int chip,INT16 **buffer,int length)
    /*TODO*///{
    /*TODO*///	struct AY8910 *PSG = &AYPSG[chip];
    /*TODO*///	INT16 *buf1,*buf2,*buf3;
    /*TODO*///	int outn;
    /*TODO*///
    /*TODO*///	buf1 = buffer[0];
    /*TODO*///	buf2 = buffer[1];
    /*TODO*///	buf3 = buffer[2];
    /*TODO*///
    /*TODO*///
    /*TODO*///	/* The 8910 has three outputs, each output is the mix of one of the three */
    /*TODO*///	/* tone generators and of the (single) noise generator. The two are mixed */
    /*TODO*///	/* BEFORE going into the DAC. The formula to mix each channel is: */
    /*TODO*///	/* (ToneOn | ToneDisable) & (NoiseOn | NoiseDisable). */
    /*TODO*///	/* Note that this means that if both tone and noise are disabled, the output */
    /*TODO*///	/* is 1, not 0, and can be modulated changing the volume. */
    /*TODO*///
    /*TODO*///
    /*TODO*///	/* If the channels are disabled, set their output to 1, and increase the */
    /*TODO*///	/* counter, if necessary, so they will not be inverted during this update. */
    /*TODO*///	/* Setting the output to 1 is necessary because a disabled channel is locked */
    /*TODO*///	/* into the ON state (see above); and it has no effect if the volume is 0. */
    /*TODO*///	/* If the volume is 0, increase the counter, but don't touch the output. */
    /*TODO*///	if (PSG->Regs[AY_ENABLE] & 0x01)
    /*TODO*///	{
    /*TODO*///		if (PSG->CountA <= length*STEP) PSG->CountA += length*STEP;
    /*TODO*///		PSG->OutputA = 1;
    /*TODO*///	}
    /*TODO*///	else if (PSG->Regs[AY_AVOL] == 0)
    /*TODO*///	{
    /*TODO*///		/* note that I do count += length, NOT count = length + 1. You might think */
    /*TODO*///		/* it's the same since the volume is 0, but doing the latter could cause */
    /*TODO*///		/* interferencies when the program is rapidly modulating the volume. */
    /*TODO*///		if (PSG->CountA <= length*STEP) PSG->CountA += length*STEP;
    /*TODO*///	}
    /*TODO*///	if (PSG->Regs[AY_ENABLE] & 0x02)
    /*TODO*///	{
    /*TODO*///		if (PSG->CountB <= length*STEP) PSG->CountB += length*STEP;
    /*TODO*///		PSG->OutputB = 1;
    /*TODO*///	}
    /*TODO*///	else if (PSG->Regs[AY_BVOL] == 0)
    /*TODO*///	{
    /*TODO*///		if (PSG->CountB <= length*STEP) PSG->CountB += length*STEP;
    /*TODO*///	}
    /*TODO*///	if (PSG->Regs[AY_ENABLE] & 0x04)
    /*TODO*///	{
    /*TODO*///		if (PSG->CountC <= length*STEP) PSG->CountC += length*STEP;
    /*TODO*///		PSG->OutputC = 1;
    /*TODO*///	}
    /*TODO*///	else if (PSG->Regs[AY_CVOL] == 0)
    /*TODO*///	{
    /*TODO*///		if (PSG->CountC <= length*STEP) PSG->CountC += length*STEP;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* for the noise channel we must not touch OutputN - it's also not necessary */
    /*TODO*///	/* since we use outn. */
    /*TODO*///	if ((PSG->Regs[AY_ENABLE] & 0x38) == 0x38)	/* all off */
    /*TODO*///		if (PSG->CountN <= length*STEP) PSG->CountN += length*STEP;
    /*TODO*///
    /*TODO*///	outn = (PSG->OutputN | PSG->Regs[AY_ENABLE]);
    /*TODO*///
    /*TODO*///
    /*TODO*///	/* buffering loop */
    /*TODO*///	while (length)
    /*TODO*///	{
    /*TODO*///		int vola,volb,volc;
    /*TODO*///		int left;
    /*TODO*///
    /*TODO*///
    /*TODO*///		/* vola, volb and volc keep track of how long each square wave stays */
    /*TODO*///		/* in the 1 position during the sample period. */
    /*TODO*///		vola = volb = volc = 0;
    /*TODO*///
    /*TODO*///		left = STEP;
    /*TODO*///		do
    /*TODO*///		{
    /*TODO*///			int nextevent;
    /*TODO*///
    /*TODO*///
    /*TODO*///			if (PSG->CountN < left) nextevent = PSG->CountN;
    /*TODO*///			else nextevent = left;
    /*TODO*///
    /*TODO*///			if (outn & 0x08)
    /*TODO*///			{
    /*TODO*///				if (PSG->OutputA) vola += PSG->CountA;
    /*TODO*///				PSG->CountA -= nextevent;
    /*TODO*///				/* PeriodA is the half period of the square wave. Here, in each */
    /*TODO*///				/* loop I add PeriodA twice, so that at the end of the loop the */
    /*TODO*///				/* square wave is in the same status (0 or 1) it was at the start. */
    /*TODO*///				/* vola is also incremented by PeriodA, since the wave has been 1 */
    /*TODO*///				/* exactly half of the time, regardless of the initial position. */
    /*TODO*///				/* If we exit the loop in the middle, OutputA has to be inverted */
    /*TODO*///				/* and vola incremented only if the exit status of the square */
    /*TODO*///				/* wave is 1. */
    /*TODO*///				while (PSG->CountA <= 0)
    /*TODO*///				{
    /*TODO*///					PSG->CountA += PSG->PeriodA;
    /*TODO*///					if (PSG->CountA > 0)
    /*TODO*///					{
    /*TODO*///						PSG->OutputA ^= 1;
    /*TODO*///						if (PSG->OutputA) vola += PSG->PeriodA;
    /*TODO*///						break;
    /*TODO*///					}
    /*TODO*///					PSG->CountA += PSG->PeriodA;
    /*TODO*///					vola += PSG->PeriodA;
    /*TODO*///				}
    /*TODO*///				if (PSG->OutputA) vola -= PSG->CountA;
    /*TODO*///			}
    /*TODO*///			else
    /*TODO*///			{
    /*TODO*///				PSG->CountA -= nextevent;
    /*TODO*///				while (PSG->CountA <= 0)
    /*TODO*///				{
    /*TODO*///					PSG->CountA += PSG->PeriodA;
    /*TODO*///					if (PSG->CountA > 0)
    /*TODO*///					{
    /*TODO*///						PSG->OutputA ^= 1;
    /*TODO*///						break;
    /*TODO*///					}
    /*TODO*///					PSG->CountA += PSG->PeriodA;
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			if (outn & 0x10)
    /*TODO*///			{
    /*TODO*///				if (PSG->OutputB) volb += PSG->CountB;
    /*TODO*///				PSG->CountB -= nextevent;
    /*TODO*///				while (PSG->CountB <= 0)
    /*TODO*///				{
    /*TODO*///					PSG->CountB += PSG->PeriodB;
    /*TODO*///					if (PSG->CountB > 0)
    /*TODO*///					{
    /*TODO*///						PSG->OutputB ^= 1;
    /*TODO*///						if (PSG->OutputB) volb += PSG->PeriodB;
    /*TODO*///						break;
    /*TODO*///					}
    /*TODO*///					PSG->CountB += PSG->PeriodB;
    /*TODO*///					volb += PSG->PeriodB;
    /*TODO*///				}
    /*TODO*///				if (PSG->OutputB) volb -= PSG->CountB;
    /*TODO*///			}
    /*TODO*///			else
    /*TODO*///			{
    /*TODO*///				PSG->CountB -= nextevent;
    /*TODO*///				while (PSG->CountB <= 0)
    /*TODO*///				{
    /*TODO*///					PSG->CountB += PSG->PeriodB;
    /*TODO*///					if (PSG->CountB > 0)
    /*TODO*///					{
    /*TODO*///						PSG->OutputB ^= 1;
    /*TODO*///						break;
    /*TODO*///					}
    /*TODO*///					PSG->CountB += PSG->PeriodB;
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			if (outn & 0x20)
    /*TODO*///			{
    /*TODO*///				if (PSG->OutputC) volc += PSG->CountC;
    /*TODO*///				PSG->CountC -= nextevent;
    /*TODO*///				while (PSG->CountC <= 0)
    /*TODO*///				{
    /*TODO*///					PSG->CountC += PSG->PeriodC;
    /*TODO*///					if (PSG->CountC > 0)
    /*TODO*///					{
    /*TODO*///						PSG->OutputC ^= 1;
    /*TODO*///						if (PSG->OutputC) volc += PSG->PeriodC;
    /*TODO*///						break;
    /*TODO*///					}
    /*TODO*///					PSG->CountC += PSG->PeriodC;
    /*TODO*///					volc += PSG->PeriodC;
    /*TODO*///				}
    /*TODO*///				if (PSG->OutputC) volc -= PSG->CountC;
    /*TODO*///			}
    /*TODO*///			else
    /*TODO*///			{
    /*TODO*///				PSG->CountC -= nextevent;
    /*TODO*///				while (PSG->CountC <= 0)
    /*TODO*///				{
    /*TODO*///					PSG->CountC += PSG->PeriodC;
    /*TODO*///					if (PSG->CountC > 0)
    /*TODO*///					{
    /*TODO*///						PSG->OutputC ^= 1;
    /*TODO*///						break;
    /*TODO*///					}
    /*TODO*///					PSG->CountC += PSG->PeriodC;
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			PSG->CountN -= nextevent;
    /*TODO*///			if (PSG->CountN <= 0)
    /*TODO*///			{
    /*TODO*///				/* Is noise output going to change? */
    /*TODO*///				if ((PSG->RNG + 1) & 2)	/* (bit0^bit1)? */
    /*TODO*///				{
    /*TODO*///					PSG->OutputN = ~PSG->OutputN;
    /*TODO*///					outn = (PSG->OutputN | PSG->Regs[AY_ENABLE]);
    /*TODO*///				}
    /*TODO*///
    /*TODO*///				/* The Random Number Generator of the 8910 is a 17-bit shift */
    /*TODO*///				/* register. The input to the shift register is bit0 XOR bit2 */
    /*TODO*///				/* (bit0 is the output). */
    /*TODO*///
    /*TODO*///				/* The following is a fast way to compute bit 17 = bit0^bit2. */
    /*TODO*///				/* Instead of doing all the logic operations, we only check */
    /*TODO*///				/* bit 0, relying on the fact that after two shifts of the */
    /*TODO*///				/* register, what now is bit 2 will become bit 0, and will */
    /*TODO*///				/* invert, if necessary, bit 16, which previously was bit 18. */
    /*TODO*///				if (PSG->RNG & 1) PSG->RNG ^= 0x28000;
    /*TODO*///				PSG->RNG >>= 1;
    /*TODO*///				PSG->CountN += PSG->PeriodN;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			left -= nextevent;
    /*TODO*///		} while (left > 0);
    /*TODO*///
    /*TODO*///		/* update envelope */
    /*TODO*///		if (PSG->Holding == 0)
    /*TODO*///		{
    /*TODO*///			PSG->CountE -= STEP;
    /*TODO*///			if (PSG->CountE <= 0)
    /*TODO*///			{
    /*TODO*///				do
    /*TODO*///				{
    /*TODO*///					PSG->CountEnv--;
    /*TODO*///					PSG->CountE += PSG->PeriodE;
    /*TODO*///				} while (PSG->CountE <= 0);
    /*TODO*///
    /*TODO*///				/* check envelope current position */
    /*TODO*///				if (PSG->CountEnv < 0)
    /*TODO*///				{
    /*TODO*///					if (PSG->Hold)
    /*TODO*///					{
    /*TODO*///						if (PSG->Alternate)
    /*TODO*///							PSG->Attack ^= 0x1f;
    /*TODO*///						PSG->Holding = 1;
    /*TODO*///						PSG->CountEnv = 0;
    /*TODO*///					}
    /*TODO*///					else
    /*TODO*///					{
    /*TODO*///						/* if CountEnv has looped an odd number of times (usually 1), */
    /*TODO*///						/* invert the output. */
    /*TODO*///						if (PSG->Alternate && (PSG->CountEnv & 0x20))
    /*TODO*/// 							PSG->Attack ^= 0x1f;
    /*TODO*///
    /*TODO*///						PSG->CountEnv &= 0x1f;
    /*TODO*///					}
    /*TODO*///				}
    /*TODO*///
    /*TODO*///				PSG->VolE = PSG->VolTable[PSG->CountEnv ^ PSG->Attack];
    /*TODO*///				/* reload volume */
    /*TODO*///				if (PSG->EnvelopeA) PSG->VolA = PSG->VolE;
    /*TODO*///				if (PSG->EnvelopeB) PSG->VolB = PSG->VolE;
    /*TODO*///				if (PSG->EnvelopeC) PSG->VolC = PSG->VolE;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		*(buf1++) = (vola * PSG->VolA) / STEP;
    /*TODO*///		*(buf2++) = (volb * PSG->VolB) / STEP;
    /*TODO*///		*(buf3++) = (volc * PSG->VolC) / STEP;
    /*TODO*///
    /*TODO*///		length--;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///void AY8910_set_clock(int chip,int clock)
    /*TODO*///{
    /*TODO*///	struct AY8910 *PSG = &AYPSG[chip];
    /*TODO*///
    /*TODO*///	/* the step clock for the tone and noise generators is the chip clock    */
    /*TODO*///	/* divided by 8; for the envelope generator of the AY-3-8910, it is half */
    /*TODO*///	/* that much (clock/16), but the envelope of the YM2149 goes twice as    */
    /*TODO*///	/* fast, therefore again clock/8.                                        */
    /*TODO*///	/* Here we calculate the number of steps which happen during one sample  */
    /*TODO*///	/* at the given sample rate. No. of events = sample rate / (clock/8).    */
    /*TODO*///	/* STEP is a multiplier used to turn the fraction into a fixed point     */
    /*TODO*///	/* number.                                                               */
    /*TODO*///	PSG->UpdateStep = ((double)STEP * PSG->SampleRate * 8) / clock;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///void AY8910_set_volume(int chip,int channel,int volume)
    /*TODO*///{
    /*TODO*///	struct AY8910 *PSG = &AYPSG[chip];
    /*TODO*///	int ch;
    /*TODO*///
    /*TODO*///	for (ch = 0; ch < 3; ch++)
    /*TODO*///		if (channel == ch || channel == ALL_8910_CHANNELS)
    /*TODO*///			mixer_set_volume(PSG->Channel + ch, volume);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    static void build_mixer_table(int chip)
    {
    /*TODO*///	struct AY8910 *PSG = &AYPSG[chip];
    /*TODO*///	int i;
    /*TODO*///	double out;
    /*TODO*///
    /*TODO*///
    /*TODO*///	/* calculate the volume->voltage conversion table */
    /*TODO*///	/* The AY-3-8910 has 16 levels, in a logarithmic scale (3dB per step) */
    /*TODO*///	/* The YM2149 still has 16 levels for the tone generators, but 32 for */
    /*TODO*///	/* the envelope generator (1.5dB per step). */
    /*TODO*///	out = MAX_OUTPUT;
    /*TODO*///	for (i = 31;i > 0;i--)
    /*TODO*///	{
    /*TODO*///		PSG->VolTable[i] = out + 0.5;	/* round to nearest */
    /*TODO*///
    /*TODO*///		out /= 1.188502227;	/* = 10 ^ (1.5/20) = 1.5dB */
    /*TODO*///	}
    /*TODO*///	PSG->VolTable[0] = 0;
    }

    void AY8910_reset(int chip)
    {
    /*TODO*///	int i;
    /*TODO*///	struct AY8910 *PSG = &AYPSG[chip];
    /*TODO*///
    /*TODO*///
    /*TODO*///	PSG->register_latch = 0;
    /*TODO*///	PSG->RNG = 1;
    /*TODO*///	PSG->OutputA = 0;
    /*TODO*///	PSG->OutputB = 0;
    /*TODO*///	PSG->OutputC = 0;
    /*TODO*///	PSG->OutputN = 0xff;
    /*TODO*///	for (i = 0;i < AY_PORTA;i++)
    /*TODO*///		_AYWriteReg(chip,i,0);	/* AYWriteReg() uses the timer system; we cannot */
    /*TODO*///								/* call it at this time because the timer system */
    /*TODO*///								/* has not been initialized. */
    }
    
    static int AY8910_init(MachineSound msound,int chip,
    		int clock,int volume,int sample_rate,
    		ReadHandlerPtr portAread,ReadHandlerPtr portBread,
    		WriteHandlerPtr portAwrite,WriteHandlerPtr portBwrite)
    {
    	int i;
        AY8910 PSG = AYPSG[chip];
    /*TODO*///	char buf[3][40];
    /*TODO*///	const char *name[3];
    /*TODO*///	int vol[3];
    /*TODO*///
    /*TODO*///
    /*TODO*///	memset(PSG,0,sizeof(struct AY8910));
    	PSG.SampleRate = sample_rate;
    	PSG.PortAread = portAread;
    	PSG.PortBread = portBread;
    	PSG.PortAwrite = portAwrite;
    	PSG.PortBwrite = portBwrite;
    /*TODO*///	for (i = 0;i < 3;i++)
    /*TODO*///	{
    /*TODO*///		vol[i] = volume;
    /*TODO*///		name[i] = buf[i];
    /*TODO*///		sprintf(buf[i],"%s #%d Ch %c",sound_name(msound),chip,'A'+i);
    /*TODO*///	}
    /*TODO*///	PSG->Channel = stream_init_multi(3,name,vol,sample_rate,chip,AY8910Update);
    /*TODO*///
    /*TODO*///	if (PSG->Channel == -1)
    /*TODO*///		return 1;
    /*TODO*///
    /*TODO*///	AY8910_set_clock(chip,clock);
    /*TODO*///	AY8910_reset(chip);
    /*TODO*///
    	return 0;
    }

    @Override
    public int start(MachineSound msound) {
    	int chip;
        AY8910interface intf = (AY8910interface)msound.sound_interface;
    
    
    	for (chip = 0;chip < intf.num;chip++)
    	{
    		if (AY8910_init(msound,chip,intf.baseclock,
    				intf.mixing_level[chip] & 0xffff,
    				Machine.sample_rate,
    				intf.portAread[chip],intf.portBread[chip],
    				intf.portAwrite[chip],intf.portBwrite[chip]) != 0)
    			return 1;
    		build_mixer_table(chip);
    	}
        return 0;
    }    
}
