/* from Andrew Scott (ascott@utkux.utcc.utk.edu) */
/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.sndhrdw;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.sound.mixer.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.platform.libc_v2.*;
import static gr.codebb.arcadeflex.v037b7.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.mame.timerH.*;
import static gr.codebb.arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.rockola.*;

public class rockola
{
	
	static final int TONE_VOLUME =25;
	static final int SAMPLE_VOLUME=25;
	
	static int tonechannels,samplechannels;
	
	static int NoSound0=1;
	static int Sound0Offset;
	static int Sound0Base;
	static int Sound0Mask;
	static int Sound0StopOnRollover;
	static int NoSound1=1;
	static int Sound1Offset;
	static int Sound1Base;
	static int Sound1Mask;
	static int NoSound2=1;
	static int Sound2Offset;
	static int Sound2Base;
	static int Sound2Mask;
	static char LastPort1;
	
	
	static byte waveform[] =
	{
		(byte)0x88, (byte)0x88, (byte)0x88, (byte)0x88, (byte)0xaa, (byte)0xaa, (byte)0xaa, (byte)0xaa,
		(byte)0xcc, (byte)0xcc, (byte)0xcc, (byte)0xcc, (byte)0xee, (byte)0xee, (byte)0xee, (byte)0xee,
		0x11, 0x11, 0x11, 0x11, 0x22, 0x22, 0x22, 0x22,
		0x44, 0x44, 0x44, 0x44, 0x66, 0x66, 0x66, 0x66
	};
	
	
	
	static String vanguard_sample_names[] =
	{
		"*vanguard",
		"explsion.wav",
		"fire.wav",
		null
	};
	public static ShStartPtr rockola_sh_start = new ShStartPtr() {
        public int handler(MachineSound msound) {
		int[] vol=new int[3];
	
		Machine.samples = readsamples(vanguard_sample_names,Machine.gamedrv.name);
	
		vol[0] = vol[1] = vol[2] = TONE_VOLUME;
		tonechannels = mixer_allocate_channels(3,vol);
		vol[0] = vol[1] = vol[2] = SAMPLE_VOLUME;
		samplechannels = mixer_allocate_channels(3,vol);
	
		NoSound0=1;
		Sound0Offset=0;
		Sound0Base=0x0000;
		NoSound1=1;
		Sound1Offset=0;
		Sound1Base=0x0800;
		NoSound2=1;
		Sound2Offset=0;
		Sound2Base=0x1000;
	
		mixer_set_volume(tonechannels+0,0);
		mixer_play_sample(tonechannels+0,new BytePtr(waveform),32,1000,true);
		mixer_set_volume(tonechannels+1,0);
		mixer_play_sample(tonechannels+1,new BytePtr(waveform),32,1000,true);
		mixer_set_volume(tonechannels+2,0);
		mixer_play_sample(tonechannels+2,new BytePtr(waveform),32,1000,true);
	
		return 0;
	}};
	static int count;
	public static ShUpdatePtr rockola_sh_update = new ShUpdatePtr() { public void handler() 
	{
		
	
	
		/* only update every second call (30 Hz update) */
		count++;
		if ((count & 1) != 0) return;
	
	
		/* play musical tones according to tunes stored in ROM */
	
		if (NoSound0==0)
		{
	 		if (memory_region(REGION_SOUND1).read(Sound0Base+Sound0Offset)!=0xff)
			{
	 			mixer_set_sample_frequency(tonechannels+0,(32000 / (256-memory_region(REGION_SOUND1).read(Sound0Base+Sound0Offset))) * 16);
	 			mixer_set_volume(tonechannels+0,100);
			}
			else
				mixer_set_volume(tonechannels+0,0);
			Sound0Offset = (Sound0Offset + 1) & Sound0Mask;
			if (Sound0Offset == 0 && Sound0StopOnRollover!=0)
				NoSound0 = 1;
		}
		else
			mixer_set_volume(tonechannels+0,0);
	
		if (NoSound1==0)
		{
			if (memory_region(REGION_SOUND1).read(Sound1Base+Sound1Offset)!=0xff)
			{
				mixer_set_sample_frequency(tonechannels+1,(32000 / (256-memory_region(REGION_SOUND1).read(Sound1Base+Sound1Offset))) * 16);
				mixer_set_volume(tonechannels+1,100);
			}
			else
				mixer_set_volume(tonechannels+1,0);
			Sound1Offset = (Sound1Offset + 1) & Sound1Mask;
		}
		else
			mixer_set_volume(tonechannels+1,0);
	
		if (NoSound2==0)
		{
			if (memory_region(REGION_SOUND1).read(Sound2Base+Sound2Offset)!=0xff)
			{
				mixer_set_sample_frequency(tonechannels+2,(32000 / (256-memory_region(REGION_SOUND1).read(Sound2Base+Sound2Offset))) * 16);
				mixer_set_volume(tonechannels+2,100);
			}
			else
				mixer_set_volume(tonechannels+2,0);
			Sound2Offset = (Sound2Offset + 1) & Sound2Mask;
		}
		else
			mixer_set_volume(tonechannels+2,0);
	} };
	
	
	
	public static WriteHandlerPtr satansat_sound0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* bit 0 = analog sound trigger */
	
		/* bit 1 = to 76477 */
	
		/* bit 2 = analog sound trigger */
		if (Machine.samples!=null && Machine.samples.sample[0]!=null)
		{
			if ((data & 0x04)!=0 && (LastPort1 & 0x04)==0)
			{
				mixer_play_sample(samplechannels+0,new BytePtr(Machine.samples.sample[0].data),
				                  Machine.samples.sample[0].length,
				                  Machine.samples.sample[0].smpfreq,
				                  false);
			}
		}
	
		if ((data & 0x08) != 0)
		{
			NoSound0=1;
			Sound0Offset = 0;
		}
	
		/* bit 4-6 sound0 volume control (TODO) */
	
		/* bit 7 sound1 volume control (TODO) */
	
		LastPort1 = (char)(data&0xFF);
	} };
	
	public static WriteHandlerPtr satansat_sound1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* select tune in ROM based on sound command byte */
		Sound0Base = 0x0000 + ((data & 0x0e) << 7);
		Sound0Mask = 0xff;
		Sound0StopOnRollover = 1;
		Sound1Base = 0x0800 + ((data & 0x60) << 4);
		Sound1Mask = 0x1ff;
	
		if ((data & 0x01) != 0)
			NoSound0=0;
	
		if ((data & 0x10) != 0)
			NoSound1=0;
		else
		{
			NoSound1=1;
			Sound1Offset = 0;
		}
	
		/* bit 7 = ? */
	} };
	
	
	public static WriteHandlerPtr vanguard_sound0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* select musical tune in ROM based on sound command byte */
	
		Sound0Base = ((data & 0x07) << 8);
		Sound0Mask = 0xff;
		Sound0StopOnRollover = 0;
	
		/* play noise samples requested by sound command byte */
		if (Machine.samples!=null && Machine.samples.sample[0]!=null)
		{
			if ((data & 0x20)!=0 && (LastPort1 & 0x20)==0)
				mixer_play_sample(samplechannels+2,new BytePtr(Machine.samples.sample[1].data),
				                  Machine.samples.sample[1].length,
				                  Machine.samples.sample[1].smpfreq,
				                  false);
			else if ((data & 0x20)==0 && (LastPort1 & 0x20)!=0)
				mixer_stop_sample(samplechannels+2);
	
			if ((data & 0x40)!=0 && (LastPort1 & 0x40)==0)
				mixer_play_sample(samplechannels+0,new BytePtr(Machine.samples.sample[1].data),
				                  Machine.samples.sample[1].length,
				                  Machine.samples.sample[1].smpfreq,
				                  false);
			else if ((data & 0x20)==0 && (LastPort1 & 0x20)!=0)
				mixer_stop_sample(samplechannels+0);
	
			if ((data & 0x80)!=0 && (LastPort1 & 0x80)==0)
			{
				mixer_play_sample(samplechannels+1,new BytePtr(Machine.samples.sample[0].data),
				                  Machine.samples.sample[0].length,
				                  Machine.samples.sample[0].smpfreq,
				                  false);
			}
		}
	
		if ((data & 0x10) != 0)
		{
			NoSound0=0;
		}
	
		if ((data & 0x08) != 0)
		{
			NoSound0=1;
			Sound0Offset = 0;
		}
	
		LastPort1 = (char)(data&0xFF);
	} };
	
	public static WriteHandlerPtr vanguard_sound1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* select tune in ROM based on sound command byte */
		Sound1Base = 0x0800 + ((data & 0x07) << 8);
		Sound1Mask = 0xff;
	
		if ((data & 0x08) != 0)
			NoSound1=0;
		else
		{
			NoSound1=1;
			Sound1Offset = 0;
		}
	} };
	
	
	
	public static WriteHandlerPtr fantasy_sound0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* select musical tune in ROM based on sound command byte */
		Sound0Base = 0x0000 + ((data & 0x07) << 8);
		Sound0Mask = 0xff;
		Sound0StopOnRollover = 0;
	
		/* play noise samples requested by sound command byte */
		if (Machine.samples!=null && Machine.samples.sample[0]!=null)
		{
			if ((data & 0x80)!=0 && (LastPort1 & 0x80)==0)
			{
				mixer_play_sample(samplechannels+0,new BytePtr(Machine.samples.sample[0].data),
				                  Machine.samples.sample[0].length,
				                  Machine.samples.sample[0].smpfreq,
				                  false);
			}
		}
	
		if ((data & 0x08) != 0)
			NoSound0=0;
		else
		{
			Sound0Offset = Sound0Base;
			NoSound0=1;
		}
	
		if ((data & 0x10) != 0)
			NoSound2=0;
		else
		{
			Sound2Offset = 0;
			NoSound2=1;
		}
	
		LastPort1 = (char)(data&0xFF);
	} };
	
	public static WriteHandlerPtr fantasy_sound1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* select tune in ROM based on sound command byte */
		Sound1Base = 0x0800 + ((data & 0x07) << 8);
		Sound1Mask = 0xff;
	
		if ((data & 0x08) != 0)
			NoSound1=0;
		else
		{
			NoSound1=1;
			Sound1Offset = 0;
		}
	} };
	
	public static WriteHandlerPtr fantasy_sound2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		rockola_flipscreen_w.handler(offset,data);
	
		/* select tune in ROM based on sound command byte */
		Sound2Base = 0x1000 + ((data & 0x70) << 4);
	//	Sound2Base = 0x1000 + ((data & 0x10) << 5) + ((data & 0x20) << 5) + ((data & 0x40) << 2);
		Sound2Mask = 0xff;
	} };
}
